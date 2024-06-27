package automata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.io.IOException;

/**
 * Abstract automaton, specifies what should be implemented for any automaton
 * type.
 */
public abstract class Automaton {
  /**
   * An interface for functions
   * {@code (String alphabet) -> new Automaton (alphabet)}.
   * 
   * @param <T> Automaton type.
   */
  public interface AutomatonBuilder<T extends Automaton> {
    public T build (String alphabet);
  }

  /**
   * Interface for automata implementing the {@code determinize} method.
   *
   * @param <DetAut> The return type of the {@code determinize} method.
   */
  public interface NonDeterminism<DetAut extends Automaton> {
    public DetAut determinize ();
  }

  public interface RegExpConverter<DetAut extends Automaton,
      NonDetAut extends Automaton & NonDeterminism<DetAut>> {
    public String NFAToRegExp (NonDetAut A);

    public NonDetAut regExpToNFA (String re, Set<Character> alphabet);

    public NonDetAut regExpToNFA (String re, String alphabet);
  }

  /**
   * The EPSILON letter, representing the empty word.
   */
  public static final char EPSILON = 'e';

  // For convenience, to avoid the super() calls.
  public Automaton () {
  }

  /**
   * Alphabet of the automaton.
   * 
   * @return The alphabet of the automaton.
   */
  public abstract Set<Character> getAlphabet ();

  /**
   * Add a state to the automaton.
   * 
   * @param q The state to add.
   */
  public abstract void addState (String q);

  /**
   * Add a transition to the automaton.
   * 
   * @param p     State from which the transition goes.
   * @param label Label of the transition.
   * @param q     State to which the transition goes.
   */
  public abstract void addTransition (String p, char label, String q);

  /**
   * Set a state to be final.
   * 
   * @param q State to add to the set of final states.
   */
  public abstract void setFinal (String q);

  /**
   * Return the set of final states.
   * 
   * @return The set of final states.
   */
  public abstract Set<String> getFinal ();

  /**
   * Set a state to be initial.
   * 
   * @param q The state to set as initial.
   */
  public abstract void setInitial (String q);

  /**
   * Get the initial state.
   * 
   * @return The initial state.
   */
  public abstract String getInitial ();

  /**
   * Return the set of states.
   * 
   * @return The set of states.
   */
  public abstract Set<String> getStates ();

  /**
   * Get all transitions from a given state.
   * 
   * @param p The state of which we want the outgoing transitions.
   * @return The set of outgoing transitions, where each outgoing transition is a
   *         pair (label, destination).
   */
  public abstract Set<Pair<Character, String>> getTransitionsFrom (String p);

  /**
   * Check whether a word is accepted by the automaton.
   * 
   * @param w An input word.
   * @return Whether the automaton accepts the input word.
   */
  public abstract boolean accepts (String w);

  /**
   * Check whether the automaton is complete.
   * 
   * @return Whether the automaton is complete, that is, for every state, there is
   *         one outgoing transition for each letter of the alphabet.
   */
  public boolean isComplete () {
    for (String q : getStates ()) {
      Set<Character> outalphabet =
          getTransitionsFrom (q).stream ()
              .map (p -> p.getKey ())
              .collect (Collectors.toSet ());
      if (!outalphabet.equals (getAlphabet ()))
        return false;
    }
    return true;
  }

  /**
   * Complete the automaton in place, adding a new state only if necessary.
   */
  public void complete () {
    assert (false);
  }

  /**
   * Check whether the automaton is deterministic, that is, for every
   * transition, there is at most one outgoing transition for each letter
   * of the alphabet.
   * 
   * @return Whether the automaton is deterministic.
   */
  public boolean isDeterministic () {
    for (String q : getStates ()) {
      List<Character> outalphabet =
          getTransitionsFrom (q).stream ()
              .map (p -> p.getKey ())
              .collect (Collectors.toList ());
      Set<Character> setoutalphabet = new HashSet<> (outalphabet);
      if (setoutalphabet.size () < outalphabet.size () ||
          !getAlphabet ().containsAll (setoutalphabet))
        return false;
    }
    return true;
  }

  /**
   * Trim the automaton, so that all states are accessible and coaccessible, with
   * the possible exception of a sink state.  There is a sink state in this automaton iff
   * a useful state had a transition to a useless state.  This ensures in particular that
   * if the starting automaton was complete, the output is automaton.
   *
   * @param <Aut>   The type of automaton in which to store the result.
   * @param builder A builder for that type.
   * @return A trim version of the current automaton, modulo a possible sink state.
   */
  public <Aut extends Automaton>
      Aut trim (AutomatonBuilder<Aut> builder) throws IOException {
    int size;

    // Compute accessible
    Set<String> accessible = new HashSet<> ();
    accessible.add (getInitial ());
    do {
      size = accessible.size ();
      Set<String> new_accessible = new HashSet<> (accessible);
      for (String q : accessible)
        for (Pair<Character, String> t : getTransitionsFrom (q))
          new_accessible.add (t.getValue ());
      accessible = new_accessible;
    } while (size != accessible.size ());

    // Compute coaccessible
    Set<String> coaccessible = new HashSet<> (getFinal ());
    do {
      size = coaccessible.size ();
      for (String q : accessible)
        for (Pair<Character, String> t : getTransitionsFrom (q))
          if (coaccessible.contains (t.getValue ()))
            coaccessible.add (q);
    } while (size != coaccessible.size ());

    accessible.retainAll (coaccessible);

    if (accessible.isEmpty ())
      throw new IOException ("Trimming an automaton for the empty language.");

    // Create all states
    Aut B = builder.build (getAlphabetAsString ());
    for (String s : accessible) {
      B.addState (s);
      if (getFinal ().contains (s))
        B.setFinal (s);
    }
    B.setInitial (getInitial ());

    // Add the existing transitions
    String sink = null;
    for (String p : accessible)
      for (Pair<Character, String> t : getTransitionsFrom (p))
        if (accessible.contains (t.getValue ()))
          B.addTransition (p, t.getKey (), t.getValue ());
        else {
          // If a transition was there before and is about to be deleted,
          // add a sink state.
          if (sink == null) {
            sink = "sink";
            while (accessible.contains (sink))
              sink = sink + "0";
            B.addState (sink);
            for (Character c : getAlphabet ())
              B.addTransition (sink, c, sink);
          }
          B.addTransition (p, t.getKey (), sink);
        }

    return B;
  }

  /**
   * Minimize the automaton, putting the result in a given automaton type.
   * <b>This assumes that the current automaton is deterministic and complete!</b>
   * 
   * @param <Aut>   The type of automaton in which to store the result.
   * @param builder A builder for that type.
   * @return A minimized version of the current automaton.
   */
  public <Aut extends Automaton>
      Aut minimize (AutomatonBuilder<Aut> builder) {
    if (!isDeterministic () || !isComplete ())
      return null;

    // Using Hopcroft algorithm.

    // Initialization.
    List<Set<String>> P = new ArrayList<> ();
    Stack<Set<String>> toTreat = new Stack<> ();
    if (!getFinal ().isEmpty ()) {
      P.add (getFinal ());
      toTreat.push (getFinal ());
    }
    Set<String> allButFinal =
        getStates ().stream ().filter (x -> !getFinal ().contains (x)).collect (Collectors.toSet ());
    if (!allButFinal.isEmpty ()) {
      P.add (allButFinal);
      toTreat.push (allButFinal);
    }

    // Main loop.
    while (!toTreat.isEmpty ()) {
      Set<String> S = toTreat.pop ();
      for (Character c : getAlphabet ()) {
        Set<String> X = new HashSet<> ();
        getStates ().forEach ( (s) -> {
          if (getTransitionsFrom (s).stream ().anyMatch (p -> p.getKey () == c && S.contains (p.getValue ())))
            X.add (s);
        });
        List<Set<String>> newP = new ArrayList<> ();
        for (Set<String> Y : P) {
          Set<String> XcapY = X.stream ().filter (Y::contains).collect (Collectors.toSet ());
          Set<String> YminX = Y.stream ().filter (x -> !X.contains (x)).collect (Collectors.toSet ());
          if (!XcapY.isEmpty () && !YminX.isEmpty ()) {
            newP.add (XcapY);
            newP.add (YminX);
            if (toTreat.contains (Y)) {
              toTreat.remove (Y);
              toTreat.add (XcapY);
              toTreat.add (YminX);
            } else if (XcapY.size () <= YminX.size ())
              toTreat.add (XcapY);
            else
              toTreat.add (YminX);
          } else
            newP.add (Y);
        }
        P = newP;
      }
    }

    // P contains the set of states, build from there.
    Aut B = builder.build (getAlphabetAsString ());
    for (int i = 0; i < P.size (); ++i) {
      B.addState ("q" + i);
      if (P.get (i).contains (getInitial ()))
        B.setInitial ("q" + i);
      if (getFinal ().contains (P.get (i).iterator ().next ()))
        B.setFinal ("q" + i);
    }

    for (int i = 0; i < P.size (); ++i) {
      for (Character c : getAlphabet ()) {
        String p = P.get (i).iterator ().next ();
        String q =
            getTransitionsFrom (p).stream ().filter (pair -> pair.getKey () == c).findFirst ().orElse (null)
                .getValue ();
        List<Set<String>> finalP = P;
        B.addTransition ("q" + i, c,
            "q" + IntStream.range (0, P.size ()).filter (j -> finalP.get (j).contains (q)).findFirst ().getAsInt ());
      }
    }

    return B;
  }

  /**
   * Test whether the current automaton is equivalent to another one.
   * <b>This assumes that both automata are minimal and complete!</b>
   * 
   * @param <Aut> Type of the other automaton.
   * @param A     The other automaton.
   * @return Whether they are isomorphic.
   */
  public <Aut extends Automaton>
      boolean isEquivalentMinimal (Aut A) throws IOException {
    if (!getAlphabet ().equals (A.getAlphabet ()) ||
        getStates ().size () != A.getStates ().size () ||
        getFinal ().size () != A.getFinal ().size ())
      return false;
    if (!isDeterministic () || !A.isDeterministic () ||
        !isComplete () || !A.isComplete ())
      return false; // Technically, I just cannot answer here.
    Map<String, String> iso = new HashMap<> ();
    Stack<String> toTreat = new Stack<> ();
    iso.put (getInitial (), A.getInitial ());
    toTreat.push (getInitial ());
    while (!toTreat.empty ()) {
      String p = toTreat.pop ();
      Set<Pair<Character, String>> neighlist1 = getTransitionsFrom (p),
          neighlist2 = A.getTransitionsFrom (iso.get (p));
      if (neighlist1 == null || neighlist2 == null)
        throw new IOException ("getTransitionsFrom returned a null.");
      for (Pair<Character, String> neigh : neighlist1) {
        String neighstateA2 = neighlist2.stream ().filter (pair -> pair.getKey () == neigh.getKey ())
          .findFirst ().orElse (new Pair<> (null, null)).getValue ();
        if (neighstateA2 == null) // Crash
          return false;
        if (!iso.containsKey (neigh.getValue ())) {
          iso.put (neigh.getValue (), neighstateA2);
          toTreat.push (neigh.getValue ());
        } else if (!iso.get (neigh.getValue ()).equals (neighstateA2))
          return false;
      }
    }
    return true;
  }

  /**
   * @return The alphabet as a string.
   */
  public String getAlphabetAsString () {
    return getAlphabet ().stream ().map (String::valueOf).collect (Collectors.joining ());
  }

  // Helper to get all transitions as (p, c, q), for .equals and .hashCode.
  private Set<Pair<String, Pair<Character, String>>> _getTransitions () {
    Set<Pair<String, Pair<Character, String>>> transitions = new HashSet<> ();
    getStates ().forEach (p -> {
      getTransitionsFrom (p).forEach (pair -> {
        transitions.add (new Pair<> (p, pair));
      });
    });
    return transitions;
  }

  @Override
  final public int hashCode () {
    return Objects.hash (getStates (), getAlphabet (), _getTransitions (),
        getInitial (), getFinal ());
  }

  @Override
  final public boolean equals (Object obj) {
    if (this == obj)
      return true;
    if (! (obj instanceof Automaton))
      return false;
    Automaton other = (Automaton) obj;
    return getStates ().equals (other.getStates ()) &&
        getAlphabet ().equals (other.getAlphabet ()) &&
        _getTransitions ().equals (other._getTransitions ()) &&
        getInitial ().equals (other.getInitial ()) &&
        getFinal ().equals (other.getFinal ());
  }
}
