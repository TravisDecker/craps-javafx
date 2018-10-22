package edu.cnm.deepdive.craps.model;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Game {

  private static final String FORMAT_STRING = "%s %s%n";
  private final Object lock = new Object();

  private State state;
  private int point;
  private Random rng;
  private List<Roll> rolls;
  private int wins;
  private int losses;

  /**
   * initalizes the win/loss tallies and the roll list.
   * @param rng
   */
  public Game(Random rng) {
    this.rng = rng;
    rolls = new LinkedList<>();
    wins = 0;
    losses = 0;
  }

  /**
   * Resets the game by setting the state to come out and pint to 0.
   */
  public void reset() {
    state = State.COME_OUT;
    point = 0;
    synchronized (lock) {
      rolls.clear();
    }
  }

  /**
   * Generates the dice roll.
   * @return
   */
  private State roll() {
    int[] dice = {
        1 + rng.nextInt(6),
        1 + rng.nextInt(6)
    };
    int total = dice[0] + dice[1];
    State state = this.state.roll(total, point);
    if (this.state == State.COME_OUT && state == State.POINT) {
      point = total;
    }
    this.state = state;
    synchronized (lock) {
      rolls.add(new Roll(dice, state));
    }
    return state;
  }

  /**
   * Sets The tallies for a new game .
   * @return
   */
  public State play() {
    reset();
    while (state != State.WIN && state != State.LOSS) {
      roll();
    }
    if (state == State.WIN) {
      wins++;
    } else {
      losses++;
    }
    return state;
  }

  /**
   * returns game state.
   * @return
   */
  public State getState() {
    return state;
  }

  /**
   * Returns the rolls list and sync locks it to prevent a read write collision.
   * @return
   */
  public List<Roll> getRolls() {
    synchronized (lock) {
      return new LinkedList<>(rolls);
    }
  }

  /**
   * returns the tally of wins.
   * @return
   */
  public int getWins() {
    return wins;
  }

  /**
   * returnd the tally of losses.
   * @return
   */
  public int getLosses() {
    return losses;
  }

  /**
   * class defines each state of the craps game.
   */
  public static class Roll {

    private final int[] dice;
    private final State state;

    private Roll(int[] dice, State state) {
      this.dice = Arrays.copyOf(dice, 2);
      this.state = state;
    }

    public int[] getDice() {
      return Arrays.copyOf(dice, 2);
    }

    public State getState() {
      return state;
    }

    @Override
    public String toString() {
      return String.format(FORMAT_STRING, Arrays.toString(dice), state);
    }

  }

  public enum State {

    COME_OUT {
      @Override
      public State roll(int total, int point) {
        switch (total) {
          case 2:
          case 3:
          case 12:
            return LOSS;
          case 7:
          case 11:
            return WIN;
          default:
            return POINT;
        }
      }
    },
    WIN,
    LOSS,
    POINT {
      @Override
      public State roll(int total, int point) {
        if (total == point) {
          return WIN;
        } else if (total == 7) {
          return LOSS;
        } else {
          return POINT;
        }
      }
    };

    public State roll(int total, int point) {
      return this;
    }

  }

}
