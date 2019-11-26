package com.robot;
/**
 * 
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Darrin Donahue
 * This is the Robot class to read input terms, find them in the dictionary and display their definitions.
 * We are converting everything to UPPERCASE to make searching easier 
 *
 */
public class RobotController {
	private static final String BASE_URL = "https://oke5yaeave.execute-api.us-west-2.amazonaws.com/prod";
	private static final String API_KEY = "gHpEGC5UID3rMIGLHLmYQ1Ihxd5Ysoiu7cKHnT7u";
	private static final String API_HEADER = "x-api-key";
    private static final String STATUS = "/status";
    private static final String QUIT = "QUIT";
	// Arms
	private static final String ARMS_NEXT_PAGE = "/move-to-next-page";
	private static final String ARMS_PREVIOUS_PAGE = "/move-to-previous-page";
	private static final String ARMS_FIRST_PAGE = "/jump-to-first-page";
	private static final String ARMS_LAST_PAGE = "/jump-to-last-page";
	// Camera
	private static final String CAMERA_NEXT_TERM = "/move-to-next-term";
	// Currently not using but including for reference
	//private static final String CAMERA_PREVIOUS_TERM = "/move-to-previous-term";
	private static final String CAMERA_FIRST_TERM = "/jump-to-first-term";
	private static final String CAMERA_LAST_TERM = "/jump-to-last-term";
    private static StringBuilder firstTerm = new StringBuilder();
    private static StringBuilder lastTerm = new StringBuilder();
    private static StringBuilder firstDefinition = new StringBuilder();
    private static StringBuilder lastDefinition = new StringBuilder();
    
	// Throttling and quotas are enabled for your API key. Requests are throttled to bursts of 1000 requests, with a refill rate of 200 requests per second. 
	// There is a quota/limit of 100,000 requests per API key per day. Please let us know if you encounter any issues with rate limiting.

	/**
	 * Entry point into the program
	 * @param args
	 */
	public static void main(String[] args) {
	    
        String input = "";
        boolean more = true;
        Scanner scanner = null;

        if(initialize()) {
            System.out.println(String.format("Enter a term for me to read (in English please not pig-latin) and press the Enter key or enter %s to quit:", QUIT));
            
            // loop until they enter QUIT
            scanner = new Scanner(System.in);
            while(more) {
                input = scanner.next().toUpperCase();
                if(input.equalsIgnoreCase(QUIT)) {
                    more = false;
                }
                else {
                    if(!lookupTerm(input)) {
                        more = false;
                    }
                }
            }
            
            scanner.close();
        }
        
		System.out.println("See ya, come back real soon now.");
	}

    /**
     * Get and store our initial data
     * @return false - error
     */
    private static boolean initialize() {
        boolean result = false;
        
        RobotStatus status = MakeRequest(STATUS, false);
        if(null == status.getError()) {
            // Get the first term in our Dictionary
            RobotStatus first = MakeRequest(ARMS_FIRST_PAGE, true);
            if(null == first.getError()) {
                RobotStatus ft = MakeRequest(CAMERA_FIRST_TERM, true);
                if(null == ft.getError()) {
                    firstTerm.append(ft.getCurrentTerm().toUpperCase());
                    firstDefinition.append(ft.getCurrentTermDefinition());
                    
                    // Get the last term in our Dictionary
                    RobotStatus last = MakeRequest(ARMS_LAST_PAGE, true);
                    if(null == last.getError()) {
                        RobotStatus lt = MakeRequest(CAMERA_LAST_TERM, true);
                        if(null == lt.getError()) {
                            lastTerm.append(lt.getCurrentTerm().toUpperCase());
                            lastDefinition.append(lt.getCurrentTermDefinition());
                            result = true; // no errors
                        }
                    }
                }
            }
        }
        return result;
    }

	/**
	 * Search until we find the entered term
	 * @param input - term to search for
	 */
	private static boolean lookupTerm(String input) {
		boolean result = true;
		
        // Let's figure out where we're at and where we need to go.  
		// If they asked for first or last term, we're good
        if(input.equalsIgnoreCase(firstTerm.toString())) {
            System.out.println(firstDefinition.toString());
        }
        else if(input.equalsIgnoreCase(lastTerm.toString())) {
            System.out.println(lastDefinition.toString());
        }
        else {
            // We need to find the input term in the dictionary.  We have 6 choices:
            // We're already here (maybe they entered the same term twice)
            // Go to first page and search.
            // Go to last page and search backwards.
            // Start here and search forwards.
            // Start here and search backwards.
            // Error
            String next = whereToNext(input);
            // Done
            if(0 == next.compareTo(QUIT)) {
                result = false;
            }
            else if(next == ARMS_PREVIOUS_PAGE || next == ARMS_LAST_PAGE 
                ||  next == ARMS_FIRST_PAGE || next == ARMS_NEXT_PAGE) {   
                // Either keep backing up until we're past it, then read the page one item at a time
                // Or go forward until we're past it, then backup 1 page and read until we find it
                result = seekUntilWeFind(next, input);
            }
        }
		return result;
	}

	/**
	 * Determine where the robot should go from here
     * We need to find the input term in the dictionary.  We have 6 choices:
     *   We're here (could happen if they enter the same term twice)
     *   Go to first page and search forwards.
     *   Go to last page and search backwards.
     *   Start here and search forwards.
     *   Start here and search backwards.
     *   Error
	 * @param input - Term to search for
	 * @return Where to go next
	 */
	private static String whereToNext(String input) {
	    
	    String next = QUIT;
	    int compare = 0;
        int current = 0;
        int cterm = 0;
        int a = Character.getNumericValue('A');
        int z = Character.getNumericValue('Z');
	    
        RobotStatus status = MakeRequest(CAMERA_FIRST_TERM, true);
        if(null != status.getError()) {
            // We got an error
            System.out.println("I don't think we can recover from this error");
        }
        else {
            // Compare the input term to the current term and determine what is the shortest route
            compare = status.getCurrentTerm().compareTo(input);
            current = Character.getNumericValue(status.getCurrentTerm().charAt(0));
            cterm = Character.getNumericValue(input.charAt(0));

            if(compare > 0) {
                // We are pointing passed the term, need to back up.  But is it closer to backup 1 page at a time or
                // to go to the beginning of the dictionary and search forwards?
                if(current - cterm < cterm - a) {
                    next = ARMS_PREVIOUS_PAGE;
                }
                else {
                    next = ARMS_FIRST_PAGE;
                }
            }
            else if(compare < 0) {
                // We are before our term and need to go forward.  But is it closer to go forward 1 page at a time or
                // to go to the end of the dictionary and search backwards?
                // Compare the first characters
                if(current - cterm < cterm - z) {
                    next = ARMS_LAST_PAGE;
                }
                else {
                    next = ARMS_NEXT_PAGE;
                }
            }
            else {
                // We're already here so we'll do them a favor and display the definition so they do not
                // have to look it up
                System.out.println(status.getCurrentTermDefinition());
                next = ""; 
            }
        }
        return next;
    }

	/**
	 * Don't rest until we find the term (or there's an error)
	 * @param type URL we're looking for
	 * @param input Term we're looking for
	 * @return false - error
	 */
	private static boolean seekUntilWeFind(String type, String input) {
	    boolean result = true;
	    // Go to a page
        RobotStatus status = MakeRequest(type, true);
        if(null != status.getError()) {
            // We got an error
            System.out.println("I don't think we can proceed");
            result = false;
        }
        else {
            // It's remotely possible this is the item and we're done.  Otherwise, keep searching
            if(input.equalsIgnoreCase(status.getCurrentTerm())) {
                System.out.println(status.getCurrentTermDefinition());
            }
            else {
                status = MakeRequest(CAMERA_FIRST_TERM, true);
                if(null != status.getError()) {
                    // We got an error
                    System.out.println("I don't think we can proceed");
                    result = false;
                }
                else {
                    switch(type) {
                    // Backup a page and begin search
                    case ARMS_PREVIOUS_PAGE:
                        result = backwardSearch(input, status);
                        break;

                    // Go to the last page and then start our search
                    case ARMS_LAST_PAGE:
                        result = backwardSearch(input, status);
                        break;
                        
                    // Go to first page and begin our search
                    case ARMS_FIRST_PAGE:
                        result = forwardSearch(input, status);
                        break;
                        
                    // Go to the next page and start our search
                    case ARMS_NEXT_PAGE:
                        result = forwardSearch(input, status);
                        break;
                        
                    default:
                        System.out.println(type + " is invalid in function seekUntilWeFind()");
                        result = false;
                        break;
                    }
                }
            }
        }

	    return result;
	}
	
    /**
     * Search forward until we go past the item.  Then backup a page and search item by item until
     * we find it.
     * @param input item to find
     * @return false - error
     */
    private static boolean forwardSearch(String input, RobotStatus status) {
        boolean result = true;
        String search = ARMS_NEXT_PAGE;

        // We'll "break" when we're done searching
        while(true) {
            // If we're already on the last page, then we're good
            if(!status.isHasNextPage()) {
                if(!searchThisPage(input)) {
                    result = false;
                }
                break;
            }
            
            // Did we go past the input?
            int compare = status.getCurrentTerm().compareTo(input);

            if (0 == compare) {
                // We found it!  We're done here
                System.out.println(status.getCurrentTermDefinition());
                break;
            }
            else if(compare > 0) {
                // We are pointing passed the term.  Back up a page and search forward, 1 item at a time,
                // until we find it
                search = ARMS_PREVIOUS_PAGE;
                status = MakeRequest(search, true);
                if(null != status.getError()) { 
                    // We got an error
                    System.out.println("I don't think we can proceed");
                    result = false;
                    break;
                }
                
                if(!searchThisPage(input)) {
                    result = false;
                }
                break;
            }
            else {
                // Go to the next page
                status = MakeRequest(search, true);
                if(null != status.getError()) {
                    // We got an error
                    System.out.println("I don't think we can proceed");
                    result = false;
                    break;
                }
                status = MakeRequest(CAMERA_FIRST_TERM, true);
                if(null != status.getError()) {
                    // We got an error
                    System.out.println("I don't think we can proceed");
                    result = false;
                    break;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Search backward until we go past the item.  Then go item by item until we find it
     * @param input item to find
     * @param status The current position
     * @return false - error
     */
    private static boolean backwardSearch(String input, RobotStatus status) {
        boolean result = true;
        String search = ARMS_PREVIOUS_PAGE;

        // We'll "break" when we're done searching
        while(true) {
            // Did we go past the input?
            int compare = status.getCurrentTerm().compareTo(input);
            if (0 == compare) {
                // We found it!  We're done here
                System.out.println(status.getCurrentTermDefinition());
                break;
            }
            else if(compare < 0) {
                // We are in front of the term so we need to search forward, 1 item at a time,
                // until we find it
                if(!searchThisPage(input)) {
                    result = false;
                }
                break;
            }
            else {
                // Keep searching and go to the previous page
                status = MakeRequest(search, true);
                if(null != status.getError()) {
                    // We got an error
                    System.out.println("I don't think we can proceed");
                    result = false;
                    break;
                }
                status = MakeRequest(CAMERA_FIRST_TERM, true);
                if(null != status.getError()) {
                    // We got an error
                    System.out.println("I don't think we can proceed");
                    result = false;
                    break;
                }
            }
        }
        
        return result;
    }
    
	/**
	 * The input is either on this page or does not exist
	 * @return false - error
	 */
	private static boolean searchThisPage(String input) {
	    boolean result = true;
	    StringBuilder previousTerm = new StringBuilder();

	    // Make sure we're on the first term
        RobotStatus status = MakeRequest(CAMERA_FIRST_TERM, true);
        if(null != status.getError()) {
            // We got an error
            System.out.println("I don't think we can proceed");
            result = false;
        }
	    
	    while(result) {
	        status = MakeRequest(CAMERA_NEXT_TERM, true);
	        if(null != status.getError()) {
	            // We got an error
	            System.out.println("I don't think we can proceed");
	            result = false;
	            break;
	        }
	        else if(status.getCurrentTerm().equalsIgnoreCase(input)) {
	            // found us!
                System.out.println(status.getCurrentTermDefinition());
                break;
	        }

	        int compare = status.getCurrentTerm().compareTo(input);
            if(compare > 0) {
                // We went past the term, therefore it does not exist
                System.out.println("So sorry, I couldn't find this term in our dictionary.");
                break;
            }
            else if(previousTerm.toString().equalsIgnoreCase(status.getCurrentTerm())) {
                // This means we searched the entire page and didn't find us.  Therefore we do not exist
                System.out.println("So sorry, I couldn't find this term in our dictionary.");
                break;
            }
            
            previousTerm.setLength(0);
            previousTerm.append(status.getCurrentTerm());
	    }

	    return result;
	}

    /**
	 * Make the request
	 * @param url - URL for us to call
	 * @param inputData - Set this data only if there's a header
	 * @return RobotStatus with response code and data
	 */
	private static RobotStatus MakeRequest(String url, boolean post) {
		
		RobotStatus robotStatus = new RobotStatus();	
		int responseCode = 0;
		HttpURLConnection connection = null;
		String readLine = null;
		
		try {
			URL urlForMakeRequest = new URL(BASE_URL + url);
		    connection = (HttpURLConnection) urlForMakeRequest.openConnection();

		    if(post) {
			    connection.setRequestMethod("POST");
		    }
		    else {
			    connection.setRequestMethod("GET");
		    }
		    
		    connection.setRequestProperty(API_HEADER, API_KEY);
		    responseCode = connection.getResponseCode();
		    if(responseCode != HttpURLConnection.HTTP_OK) {
		    	// Couldn't get connected
		    	robotStatus.setError("Couldn't connect to " + url);
	            System.out.println("Couldn't connect to " + url);
		    }
		    else {
	    		// Automatically closes everything for us
		        try(BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
			        StringBuilder data = new StringBuilder();
			        while ((readLine = in.readLine()) != null) {
			            data.append(readLine);
			        }

			        GsonBuilder builder = new GsonBuilder(); 
				    builder.setPrettyPrinting(); 
				    Gson gson = builder.create(); 
			        robotStatus = gson.fromJson(data.toString(), RobotStatus.class);
			        
			        // If the Status is not READY, then we should try again because we might need to checkout another Robot
			        if(!robotStatus.getStatus().equalsIgnoreCase("READY")) {
			            robotStatus = MakeRequest(url, post);			            
			        }
		        }
		        catch(IOException e) {
		        	// Just throw it and the other catch will take care of it for us
		        	throw e;
		        }
	    	}
		} catch (IOException e) {
			System.out.println(e.getStackTrace());
			robotStatus.setError(e.getMessage());
		}
		finally {
			if(null != connection) {
				connection.disconnect();
			}
		}

	    return robotStatus;
	}	
}
