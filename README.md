This project presents working implementations of several reinforcement learning algorithms including the
Monte Carlo method and temporal difference methods, "Sarsa" and Q Learning, as described in
Sutton & Barto's Reinforcement Learning book.

The book presents a reinforcement learning task in Chapter 5, Exercise 5.12 called
"driving a race car around a track" which was the original motivation for the code. 

Unlike the original task, a few simplifications were made so that the task in this project is more akin to
the "gridworld" task. State is composed of only position and doesn't incorporate velocity. 
The car doesn't restart at starting position upon hitting a wall nor is there a random acceleration failure.

This project is written in Kotlin to maximize readability and conceptual capture of concepts in the book.


## The Task

![Truncated_Monte_Carlo_Demo](/images/truncated_mc_demo.gif)

A car starts at the lower left and learns to drive to the upper right goal.   
At each time step, the car can choose to go left, right, top, or down.

Starting initially with a random policy, the car makes a random walk around the "track" (board/environment) 
until encountering the upper right goal.

- Bumping into a wall yields a reward of -1.5.
- Reaching the goal gives a reward of 1.0.
- Moving from cell to cell gives a reward of -1.0
- Max number steps = 10000          

We may evaluate the performance of the car by the length of its trajectory to go from start to finish.


## Inteface

1) Use "Auto Run Episode" to have the agents automatically run one
episode after another. As agents are run, they update their polices.
2) Use "Auto Step" to watch the agent make decisions in a single episode.

As you train, you can play around with adjusting the parameters. 

For example, increasing epsilon and increasing alpha momentarily can help an agent 
get out of a learned suboptimal policy. 

Decreasing epsilon and decreasing alpha can fine-tune the estimate of a policy. 

Setting alpha to 1.0 can functionally wipe out the "memory" of an agent. 

Setting epsilon to 1.0 means an agent will choose actions completely at random. Since an episode doesn't
end until the car reaches its destination, it is statistically possible for the agent to loop forever, thus
episodes lasting more than 10000 steps automatically terminated. 

This can occur because sometimes agents wind up stuck in "cycles". But as long as epsilon is > 0 then after a while,
the agent can "escape" the cycle (say after 1000 steps) and can go on to hopefully reach the terminal state. 

The yellow arrows are there to show the underlying max Q actions. When an agent chooses an action
contrary to the action suggested by its underlying policy, an "ε" is shown on the cell.

Here's a cutoff example output running the Q Learning Agent:

**100 Episodes**
![Q_Learning 100_Episodes](/images/qlearning_100.jpg)

**500 Episodes**
![Q_Learning 500_Episodes](/images/qlearning_500.jpg)

**1000 Episodes**
![Q_Learning 1000_Episodes](/images/qlearning_1000.jpg)


## Explanations

With more exploration, the car gets a better understanding of which "state-actions" are "good" and will chose actions
which will lead it to those states. Since the only good state is the ending cell on the upper right, over time, 
the agents will learn to choose actions oriented towards reaching the ending cell.

Using the Monte Carlo method, we sample trajectories according to a policy many times to derive estimates for 
the values of state-actions. Using these estimates, the policy may be adjusted.

The monte carlo agent here uses a recency weighted average of rewards (fixed alpha vs decaying alpha) 
rather than a strict average over all rewards.

Sarsa and Q Learning updates the values of state-actions without needing a full trajectory, making updates based on
temporally local information. Information near terminal states get propagated back to the start state with enough
training.

As long as we can sample long enough, we can arrive at an optimal policy counting on 
the occasional random exploration (epsilon) to "escape" suboptimal policies.

Sarsa and Q Learning can be shown experimentally here to get better policies earlier than Monte Carlo 

## Possible Optimizations and Lessons

##### Trajectory lottery

The monte carlo method is slow here because the agent has to perform a literal **random walk** until it gets lucky at
finding the terminal state. At each cell, if there is a 25% chance of finding the right action, then aggregated
together to find any single correct path yields an extremely low probability... As the maze gets bigger, the
task looks more and more like a lottery!

When I made the race track without the maze-like barriers, the agents ran much faster. Adding the walls
**dramatically** reduced the probability of finding a successful trajectory (going from start to termination state). 
It's only when there is a successful trajectory does the agent learn where to go, 
otherwise it can just loop forever avoiding the walls. 

Here's another way to think of it... the car tends towards cells that has a lower probability of looping forever
under a random policy. This is almost invariant to any learned policy because of the ease at which the agent learns
looping policies - which only grows larger as the size the board increases.

The closer the car is to the ending cell, the higher the probability of the car accidentally crashing into the termination
state. 

In general, in a task with a large number of states and where there is a chief favorable terminating state,  
the monte carlo agent tends towards states where it is easier to get lucky at arriving at the favorable terminal state.

Since each cell in the race track is indistinguishable in features, there is no signal
about which cell is good or bad initially for the agent. Intuitively for humans, 
we can see there is a difference between different cells, but the agent doesn't have this
information so it appears to walk around blindly.
 
In effect, the agent performs a "flood fill" of a sort in order
to find the terminal state. 

Since the flood fill isn't structured, many states a repeated over and over. Observing this inefficiency gave me 
renewed appreciation for the N-armed bandit problem. Agents can balance exploration and exploitation better 
with sampling methods that grants more careful treatment of infrequently taken actions. 

##### Cycles, Every Visit vs First Visit, and Alpha

The every-visit Monte Carlo variant probably makes more sense than first-visit for this specific problem
where we want to minimize trajectory length and care chiefly about arriving at a terminal state. We only care about
the latest return value closest to termination rather than the return value after looping a bunch of cycles only
to arrive at the same state.

I find that the Monte Carlo agent sometimes get stuck in suboptimal policies (wrt trajectory length)
because of first-visit. It chooses to pick the suboptimal action every time and counts on the exploration factor
kicking in to escape it to find the terminal reward. With a 1.0 gamma (no discounting), 
there is little punishment made to doing this since the end return is the same regardless of trajectory length. 

One reason this can suboptimal actions may become learned is that other actions have led to future cycles. 
Because each movement on the race track has a -1.0 reward, the magnitude of some returns may be overly inflated. 
If the alpha is very small, the agent will have a longer time unlearning cycles even with a high epsilon. 

## What's missing

- Importance sampling or various other discounting methods described in Chapter 5.
- N Step methods
- Eligibility traces
- Policy gradient method  