package automata;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of deterministic automata.
 */
public class DetAutomaton extends Automaton
{
// Suggested:
private Map<Pair<String, Character>, String> delta;
private Set<String> states;
private Set<String> finalStates;
private String initialState;
private Set<Character> alphabet;

/**
 * Create an automaton on a given alphabet.
 * 
 * @param alph The alphabet of the automaton, can be EMPTY_SET.
 */
public DetAutomaton(Set<Character> alph)
	{
	// TODO: Fill this.
	alphabet = alph;

	delta = new HashMap<>();
	states = new HashSet<>();
	finalStates = new HashSet<>();

	}

/**
 * Create an automaton on a given alphabet.
 * 
 * @param alph The alphabet of the automaton, as a String, each character being
 *             a letter of the alphabet.
 */
public DetAutomaton(String alph)
	{
	// Converts a string to a set of character. Do not touch.
	this(alph.chars().mapToObj(e -> (char) e).collect(Collectors.toSet()));
	}

/**
 * Alphabet of the automaton.
 * 
 * @return The alphabet of the automaton.
 */
@Override
public Set<Character> getAlphabet()
	{
	// TODO: Fill this.
	return alphabet;
	}

/**
 * Add a state to the automaton.
 * 
 * @param q The state to add.
 */
@Override
public void addState(String q)
	{
	// TODO: Fill this.
	states.add(q);
	}

/**
 * Add a transition to the automaton.
 * 
 * @param p     State from which the transition goes.
 * @param label Label of the transition.
 * @param q     State to which the transition goes.
 */
@Override
public void addTransition(String p, char label, String q)
	{
	// TODO: Fill this.
	Pair<String, Character> p1 = new Pair<String, Character>(p, label);
	delta.put(p1, q);
	}

/**
 * Check whether a word is accepted by the automaton.
 * 
 * @param w An input word.
 * @return Whether the automaton accepts the input word.
 */
@Override
public boolean accepts(String w)
	{
	// TODO: Fill this.
	char c;
	String currState = initialState;
	for (int i = 0; i < w.length(); i++)
		{
		c = w.charAt(i);
		Pair<String, Character> p1 = new Pair<String, Character>(currState, c);
		currState = delta.get(p1);
		}
	
	for (String s : finalStates)
		{
		if (s.equals(currState))
			{
//			System.out.println("s: " + s + "currState: " + currState);
			return true;
			}
		}
//	System.out.println( "currState: " + currState);
	return false;
	}

/**
 * Set a state to be final.
 * 
 * @param q State to add to the set of final states.
 */
@Override
public void setFinal(String q)
	{
	// TODO: Fill this.
	finalStates.add(q);
	}

/**
 * Return the set of final states.
 * 
 * @return The set of final states.
 */
@Override
public Set<String> getFinal()
	{
	// TODO: Fill this.
	return finalStates;
	}

/**
 * Set a state to be initial.
 * 
 * @param q The state to set as initial.
 */
@Override
public void setInitial(String q)
	{
	// TODO: Fill this.
	initialState = q;
	}

/**
 * Get the initial state.
 * 
 * @return The initial state.
 */
@Override
public String getInitial()
	{
	// TODO: Fill this.
	return initialState;
	}

/**
 * Return the set of states.
 * 
 * @return The set of states.
 */
@Override
public Set<String> getStates()
	{
	// TODO: Fill this.
	return states;
	}

/**
 * Get all transitions from a given state.
 * 
 * @param p The state of which we want the outgoing transitions.
 * @return The set of outgoing transitions, where each outgoing transition is a
 *         pair (label, destination).
 */
@Override
public Set<Pair<Character, String>> getTransitionsFrom(String p)
	{
	// TODO: Fill this.

	Set<Pair<Character, String>> transitions = new HashSet<>();

	Set<Pair<String, Character>> keys = delta.keySet();
	Iterator<Pair<String, Character>> keysIter = keys.iterator();
	while (keysIter.hasNext())
		{
		Pair<String, Character> tempPair = keysIter.next();
		if (tempPair.getKey() == p)
			{
			String tempP = delta.get(tempPair);
			Pair<Character, String> p1 = new Pair<Character, String>(tempPair.getValue(), tempP);
			transitions.add(p1);
			}
		}

	return transitions;
	}

/**
 * Complete the automaton in place, adding a new state only if necessary.
 */
@Override
public void complete()
	{
	// TODO: Fill this. (BONUS)

	if (!isComplete())
		{
		String sink = "SinkState";
		while (states.contains(sink))
			{
			sink = sink + "0";
			}
		states.add(sink);

		Iterator<Character> alphaIter = alphabet.iterator();
		for (int i = 0; i < alphabet.size(); i++)
			{
			Character tempAlpha = alphaIter.next();
			Pair<String, Character> p1 = new Pair<String, Character>(sink, tempAlpha);
			delta.put(p1, sink);
			}

		Iterator<String> statesIter = states.iterator();

		while (statesIter.hasNext())
			{

			String tempState = statesIter.next();
			// System.out.println("tempState : " + tempState);

			alphaIter = alphabet.iterator();

			for (int i = 0; i < alphabet.size(); i++)
				{
				Character tempAlpha = alphaIter.next();
				Pair<String, Character> p1 = new Pair<String, Character>(tempState, tempAlpha);

				if (delta.get(p1) == null)
					{
					delta.put(p1, sink);
					addTransition(tempState, tempAlpha, sink);
					}

				}

			}

		}


	}
}