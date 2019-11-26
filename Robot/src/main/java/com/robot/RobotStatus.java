package com.robot;

/**
 * @author Darrin Donahue
 * This class is the Robot Status for each call
 *
 */
public class RobotStatus {
	private String status;
	private int timeUsed;
	private int timeRemaining;
	private String currentTerm;
	private String currentTermDefinition;
	private int currentPageIndex;
	private int currentTermIndex;
	private boolean hasNextPage;
	private boolean hasPreviousPage;
	private boolean hasNextTerm;
	private boolean hasPreviousTerm;
	private String error;


	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return the timeUsed
	 */
	public int getTimeUsed() {
		return timeUsed;
	}
	/**
	 * @param timeUsed the timeUsed to set
	 */
	public void setTimeUsed(int timeUsed) {
		this.timeUsed = timeUsed;
	}
	/**
	 * @return the timeRemaining
	 */
	public int getTimeRemaining() {
		return timeRemaining;
	}
	/**
	 * @param timeRemaining the timeRemaining to set
	 */
	public void setTimeRemaining(int timeRemaining) {
		this.timeRemaining = timeRemaining;
	}
	/**
	 * @return the currentTerm
	 */
	public String getCurrentTerm() {
		return currentTerm;
	}
	/**
	 * @param currentTerm the currentTerm to set
	 */
	public void setCurrentTerm(String currentTerm) {
		this.currentTerm = currentTerm;
	}
	/**
	 * @return the currentTermDefinition
	 */
	public String getCurrentTermDefinition() {
		return currentTermDefinition;
	}
	/**
	 * @param currentTermDefinition the currentTermDefinition to set
	 */
	public void setCurrentTermDefinition(String currentTermDefinition) {
		this.currentTermDefinition = currentTermDefinition;
	}
	/**
	 * @return the currentPageIndex
	 */
	public int getCurrentPageIndex() {
		return currentPageIndex;
	}
	/**
	 * @param currentPageIndex the currentPageIndex to set
	 */
	public void setCurrentPageIndex(int currentPageIndex) {
		this.currentPageIndex = currentPageIndex;
	}
	/**
	 * @return the currentTermIndex
	 */
	public int getCurrentTermIndex() {
		return currentTermIndex;
	}
	/**
	 * @param currentTermIndex the currentTermIndex to set
	 */
	public void setCurrentTermIndex(int currentTermIndex) {
		this.currentTermIndex = currentTermIndex;
	}
	/**
	 * @return the hasNextPage
	 */
	public boolean isHasNextPage() {
		return hasNextPage;
	}
	/**
	 * @param hasNextPage the hasNextPage to set
	 */
	public void setHasNextPage(boolean hasNextPage) {
		this.hasNextPage = hasNextPage;
	}
	/**
	 * @return the hasPreviousPage
	 */
	public boolean isHasPreviousPage() {
		return hasPreviousPage;
	}
	/**
	 * @param hasPreviousPage the hasPreviousPage to set
	 */
	public void setHasPreviousPage(boolean hasPreviousPage) {
		this.hasPreviousPage = hasPreviousPage;
	}
	/**
	 * @return the hasNextTerm
	 */
	public boolean isHasNextTerm() {
		return hasNextTerm;
	}
	/**
	 * @param hasNextTerm the hasNextTerm to set
	 */
	public void setHasNextTerm(boolean hasNextTerm) {
		this.hasNextTerm = hasNextTerm;
	}
	/**
	 * @return the hasPreviousTerm
	 */
	public boolean isHasPreviousTerm() {
		return hasPreviousTerm;
	}
	/**
	 * @param hasPreviousTerm the hasPreviousTerm to set
	 */
	public void setHasPreviousTerm(boolean hasPreviousTerm) {
		this.hasPreviousTerm = hasPreviousTerm;
	}
	/**
	 * @return the error
	 */
	public String getError() {
		return error;
	}
	/**
	 * @param error the error to set
	 */
	public void setError(String error) {
		this.error = error;
	}
}
