package automata;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class NonDetAutomaton extends Automaton implements Automaton.NonDeterminism<DetAutomaton>
{
	// Suggested:
	private Map<Pair<String, Character>, Set<String>> delta;
	// private Map<Pair<String, Character>, String> delta;
	private Set<String> states;
	private Set<String> finalStates;
	private String initialState;
	private Set<Character> alphabet;

	/**
	 * Create an automaton on a given alphabet. {@link EPSILON} will never be part of the input
	 * alphabet.
	 * 
	 * @param alph The alphabet of the automaton, can be EMPTY_SET.
	 */
	public NonDetAutomaton(Set<Character> alph)
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
	 * @param alph The alphabet of the automaton, as a String, each character being a letter of the
	 *             alphabet.
	 */
	public NonDetAutomaton(String alph)
		{
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
	 * @param label Label of the transition, can be {@link EPSILON}.
	 * @param q     State to which the transition goes.
	 */
	@Override
	public void addTransition(String p, char label, String q)
		{
		// TODO: Fill this.
		// /!\ label can be Automaton.EPSILON;
		Pair<String, Character> p1 = new Pair<>(p, label);
		Set<String> qSet = delta.get(p1);
		if (qSet == null)
			{
			qSet = new HashSet<>();
			}
		qSet.add(q);
		delta.put(p1, qSet);
		}

	// Suggested: helper function, closure under epsilon transitions.
	private Set<String> E(Set<String> S)
		{
		// TODO: Fill this.

		// create new set ret
		// for each element r in S
		// ret = ret + {r} + delta{r, epsilon}
		// if ret.size > S.size (set got bigger)
		// return E(ret); (do it again recursively (bigger set means new base set to
		// test))
		// return ret (base case, nothing added)
		Set<String> ret = new HashSet<>();
		for (String r : S)
			{
			Pair<String, Character> p1 = new Pair<>(r, Automaton.EPSILON);
			ret.add(r);
			Set<String> tempSet = delta.get(p1);
			if (tempSet != null)
				{
				for (String s : tempSet)
					{
					ret.add(s);
					}
				}
			}
		if (ret.size() > S.size())
			{
			return E(ret);
			}
		return ret;
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
		// System.out.println("start accepts");
		// TODO: Fill this.
		// Initialize the set of current states S with E(q0), then for each letter ℓ of
		// the input, compute N to be the set of states that can be reached from reading
		// ℓ, and update S with E(N). The word is accepted if the set of current states
		// S at the end of the execution contains a final state.
		Set<String> currStates = new HashSet<>();
		Set<Pair<Character, String>> transitionsFrom = new HashSet<>();
		Set<String> reachableStates = new HashSet<>();
		currStates.add(initialState);
		// System.out.println("initState: " + currStates);
		currStates = E(currStates);
		// System.out.println("initStates: " + currStates);

		for (int i = 0; i < w.length(); i++)
			{
			reachableStates.clear();

			// System.out.println("w: " + w.charAt(i));
			for (String s : currStates)
				{
				transitionsFrom = getTransitionsFrom(s);

				// System.out.println("s: " + s);
				// System.out.println("transitionsFrom: " + transitionsFrom);

				for (Pair<Character, String> p : transitionsFrom)
					{
					// System.out.println("p.getValue: " + p.getValue());
					if (p.getKey().equals(w.charAt(i)))
						reachableStates.add(p.getValue());
					}
				}
			currStates = E(reachableStates);
			// System.out.println(currStates);
			// currStates.addAll(E(reachableStates));

			}
		for (String s0 : currStates)
			{
			for (String s1 : finalStates)
				{
				if (s1.equals(s0))
					{
					return true;
					}
				}
			}
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
	 * @return The set of outgoing transitions, where each outgoing transition is a pair (label,
	 *         destination).
	 */
	@Override
	public Set<Pair<Character, String>> getTransitionsFrom(String p)
		{
		// TODO: Fill this.
		// System.out.println("getTransitionsFrom " + p);
		Set<Pair<Character, String>> transitions = new HashSet<>();

		// private Map<Pair<String, Character>, Set<String>> delta;
		for (Pair<String, Character> allPairs : delta.keySet())
			{
			// System.out.println("allPairs.getKey(): " + allPairs.getKey() + " p: " + p);
			if (allPairs.getKey().equals(p))
				{
				// System.out.println("allPairs.getKey() == p");
				Set<String> tempDestinations = delta.get(allPairs);
				for (String s : tempDestinations)
					{
					Pair<Character, String> p1 = new Pair<Character, String>(allPairs.getValue(), s);
					// System.out.println(" allPairs: " + allPairs + "\n s: " + s + "\n p1 : " +
					// p1);
					transitions.add(p1);
					}
				}
			}

		return transitions;
		}

	/**
	 * Determinize an automaton.
	 * 
	 * @return An equivalent deterministic automaton.
	 */
	@Override
	public DetAutomaton determinize()
		{
		// System.out.println("enter");
		// set Language
		DetAutomaton detAutomaton = new DetAutomaton(getAlphabet());
		Set<String> reachableStates = new HashSet<>();
		String reachableString;
		boolean popped = false;
		boolean dupe = false;
		Set<Set<String>> permaStateSets = new HashSet<>();
		Queue<Set<String>> stateSets = new LinkedList<Set<String>>();
		Set<String> stateSet = new HashSet<>();
		Set<String> initialSet = E(Collections.singleton(initialState));
		String initialString = setToString(initialSet);
		stateSets.add(initialSet);
		permaStateSets.add(initialSet);

		// set q0
		detAutomaton.setInitial(initialString);
		Set<Character> charset = new HashSet<>();
		charset = detAutomaton.getAlphabet();
		// set Q and delta
		// until no more new state sets are added
		// while (!initialSet.equals(reachableStates))
		while (stateSets.size() != 0)
			{

			stateSet = stateSets.remove();
			// for each letter of the alphabet
			for (Character c : charset)
				{
				// check transitions from each state of the state set
				reachableStates.clear();
				for (String s0 : stateSet)
					{
					Set<Pair<Character, String>> transitionsFrom = new HashSet<>();
					transitionsFrom = getTransitionsFrom(s0);
					for (Pair<Character, String> p : transitionsFrom)
						{
						if (c.equals(p.getKey()))
							{
							// System.out.println("s0: " + s0 + " p: " + p + " c: " + c);
							reachableStates.add(p.getValue());
							}
						}
					}

				if (!reachableStates.isEmpty())
					{
					// add states and transitions to detAutomaton
					reachableStates = E(reachableStates);
					// System.out.println("reachable states: " + reachableStates + "permastates: " +
					// permaStateSets);

					for (String s : finalStates)
						{
						if (reachableStates.contains(s))
							{
							// System.out.println("reachableStates: " + reachableStates + "s: " + s);
							detAutomaton.setFinal(setToString(reachableStates));
							}
						}

					for (Set<String> s0 : permaStateSets)
						{
						// System.out.println("s0: " + s0 + " permaStateSets: " + permaStateSets + "
						// reachableStates "
						// + reachableStates);
						if (s0.equals(reachableStates))
							{
							// System.out.println("s0: " + s0 + "s1: " + reachableStates);
							dupe = true;
							}
						}
					Set<String> tempSet = new HashSet<>();
					for (String s : reachableStates)
						{
						tempSet.add(s);
						}
					if (!dupe)
						{
						// stateSets.add(tempSet);
						}

					permaStateSets.add(tempSet);
					dupe = false;

					reachableString = setToString(reachableStates);
					detAutomaton.addState(reachableString);
					detAutomaton.addTransition(setToString(stateSet), c, reachableString);
					// System.out.println("transition added: " + setToString(stateSet) + c + reachableString);
					}
				// System.out.println("stateSets: " + stateSets + " permaStateSets: " + permaStateSets
				// + " reachableStates " + reachableStates);
				}
			// System.out.println("2:) Initial Set: " + initialSet + "\nreachableStates: " + reachableStates);
			}

		detAutomaton.complete();
		// System.out.println("detAutomaton alphabet: " + detAutomaton.getAlphabetAsString() + " \ndet init:
		// "
		// + detAutomaton.getInitial() + " \nfinal: " + detAutomaton.getFinal() + " \nstates: "
		// + detAutomaton.getStates());

		return detAutomaton;
		}

	String setToString(Set<String> S)
		{
		return S.stream().sorted(Comparator.comparing(String::toString))
				.collect(Collectors.joining(", ", "[", "]"));
		}

	// static public void main(String[] args)
	// {
	// NonDetAutomaton A = new NonDetAutomaton("ab");
	// A.addState("q0");
	// A.addState("q1");
	// A.setInitial("q0");
	// A.setFinal("q1");
	// A.addTransition("q0", 'a', "q0");
	// A.addTransition("q0", 'b', "q0");
	// A.addTransition("q0", 'b', "q1");
	// A.addTransition("q1", 'a', "q1");
	// A.addTransition("q1", 'b', "q1");
	// A.addTransition("q1", Automaton.EPSILON, "q1");
	// System.out.println("A should accept aba: " + A.accepts("aba"));
	// System.out.println("A should reject aaa: " + A.accepts("aaa"));
	// DetAutomaton Adet = A.determinize();
	// System.out.println("Adet should accept aba: " + Adet.accepts("aba"));
	// System.out.println("Adet should reject aaa: " + Adet.accepts("aaa"));
	// }
}