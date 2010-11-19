/**
 * 
 */
package de.teunito.android.cyclelife.model;



/**
 * @author teunito
 * 
 */
public interface IObservable {

	/**
	 * Add an observer to a list of observers. Observers are notified each time
	 * an event occurs that changes the state of the observable.
	 * 
	 * @param ob - The observer to add.
	 */
	public void addObserver(IObserver ob);

	/**
	 * Remove a specific observer from the list of observers.
	 * 
	 * @param ob
	 *            - The observer to remove
	 */
	public void removeObserver(IObserver ob);

	/**
	 * Remove all observer from the list of observers.
	 */
	public void removeObservers();

	/**
	 * calls the update() method for every observer in the list of observers
	 * 
	 */
	public void notifyObservers(TrackInfo trackInfo);

}
