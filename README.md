A repo for programming exercises to help me learn from Sutton & Barto's Reinforcement Learning book 

This project presents implementations of several reinforcement learning algorithms using
Monte Carlo methods and temporal difference methods including "Sarsa" and Q Learning as described in the book.

With Monte Carlo methods, through random trajectory sampling, we can derive a usable estimate for the value of actions 
for many states and thus get a policy.

Sarsa and Q Learning updates the values of state-actions without needing a full trajectory, restricting updates to
temporally local information.

With any of these methods, an optimal policy can be found as long as we can sample long enough.

With the current implementation, I am seeing that both Sarsa and Q Learning are dramatically faster than the Monte Carlo
method. I'm not sure if I've made a mistake.


### The Task

The book presents a reinforcement learning task in Chapter 5, Exercise 5.12
**Driving a race car around a track**

There is a track bounded by walls ("@").
A car in the track starts at the lower left ("1")
and there is a goal in the upper right ("2") that yields a positive reward.

            @@@@@@@@@@@@@@@@@@@@@@
            @            @@    2 @
            @            @@      @
            @            @@      @
            @            @@      @
            @                    @
            @                    @
            @                    @
            @     @@             @
            @     @@             @
            @ 1   @@             @
            @@@@@@@@@@@@@@@@@@@@@@


At each time step, the car can choose to go left, right, top, or down.
Starting initially with a random policy, the car makes a random walk around the track until encountering the
upper right goal.

With more exploration, the car gets a better understanding of which states are good.
After 6400 episodes, we decrease the car's policy's randomness to select for the best actions.
At 8000 episodes, we yield a car that has found a fairly short trajectory to the goal.

This project is written in Kotlin to maximize readability and conceptual capture of concepts in the book.

A few simplifications were made to the problem in the book, such as having State only take position
and not also velocity. The car doesn't restart at starting position upon hitting a wall
nor is there a random acceleration failure.

I might add these later :)

Here's an cutoff example output running the Monte Carlo Agent:

```

============= EPISODE 0 =====

@@@@@@@@@@@@@@@@@@@@@@
@<<<<<↑<<  ↓<@@    2 @
@↑<>>↑↓↑↑↓<>>@@    ↑ @
@<>↓<<><↓>><↑@@    ><@
@↑↑<↑↑↓↑↑↑↓>↑@@<>>><>@
@>↓><<>↓<↓↓↓<><>↓<>>>@
@↓<<>↑><↓↑<↑↑↓<><<<<↑@
@>↑↑↓>>↑<<↑<>↑↑>↑↓<↓↓@
@<<>↑↑@@↓>↓↑↑↓<<↑<↓<↓@
@↓<↓↓↑@@↑↓↑↑↓>↑↑↓<<↓↓@
@↓><<↓@@>↓><>↓↑↓>>↑<↑@
@@@@@@@@@@@@@@@@@@@@@@

Trajectory: 2154 Steps

epsilon: 0.6
gamma: 1.0

============= EPISODE 80 =====

@@@@@@@@@@@@@@@@@@@@@@
@            @@ >>>2 @
@        >↓  @@ ↑<↓  @
@   ↓  ↓ ↑↑  @@   ↑  @
@  >↑↓>↑>↑↓↓ @@   ↑  @
@ ><↑↑< ↓>↑↑↓<    ↑  @
@  ↑↓>< ↑↑↓↓><    ↑  @
@  ↑↓↓  ↑<↑↓↓↑ ↓ ↓>< @
@ >↑><@@↓↓<↓<↓↓↑>↓<>>@
@ >↑<>@@↓<↓↓↑↑<↑<↑>< @
@ ↑   @@↓<>><↑↑↓ >>< @
@@@@@@@@@@@@@@@@@@@@@@

Trajectory: 405 Steps

Max starting state return:
-783.375

epsilon: 0.6
gamma: 1.0


============= EPISODE 1600 =====

@@@@@@@@@@@@@@@@@@@@@@
@            @@ >↓ 2 @
@↓↓↓         @@><↓ ↑ @
@<↑>>><      @@< ↑ ↑ @
@↓   ↓ >>>>↓ @@↑<>↓↑ @
@<   ↓↓↑   >>↑>↑↑↓<↓ @
@↑<↓↓↓↑><    ↑   ↑↓↑ @
@ >↓↑↑↑           >↑ @
@>>↑><@@             @
@>><< @@             @
@↑↑↑  @@             @
@@@@@@@@@@@@@@@@@@@@@@

Trajectory: 204 Steps

Max starting state return:
-334.23121387283237

epsilon: 0.6
gamma: 1.0

============= EPISODE 2400 =====

@@@@@@@@@@@@@@@@@@@@@@
@       ↓    @@  >↑2<@
@      >↑↓↓  @@<>↑↓>↑@
@   ↓<↓↓>↑↓  @@>< ↑  @
@   ↓>><<><  @@↑<    @
@  >↑>↑ ↑  >>↓>↓↑<<  @
@   >↑↓  ↓>↑ ↑ ><>↑  @
@ >>>><><↓↑          @
@↓↑ ><@@↑↑           @
@↑  ↑ @@             @
@↓<   @@             @
@@@@@@@@@@@@@@@@@@@@@@

Trajectory: 181 Steps

Max starting state return:
-285.78526890101324

epsilon: 0.6
gamma: 1.0

============= EPISODE 4000 =====

@@@@@@@@@@@@@@@@@@@@@@
@            @@ ><<2 @
@            @@  ↑<↑ @
@            @@   ↑  @
@            @@   ↑  @
@                 ↑  @
@ >< ><           ↑><@
@ >↑><<>>↓        >↑ @
@>↓↓>>@@ ↓  ><↓   ↑  @
@↑>↓<>@@↓<>><<↓   ↑  @
@<<<>>@@↓↓↑><<<>>↓↑  @
@@@@@@@@@@@@@@@@@@@@@@

Trajectory: 225 Steps

Max starting state return:
-225.55301602262017

epsilon: 0.6
gamma: 1.0

============= EPISODE 6400 =====

@@@@@@@@@@@@@@@@@@@@@@
@            @@ ><>2 @
@            @@  ↑<  @
@            @@   ↑  @
@            @@   ↑  @
@                 ↑  @
@   >↓↓           ↑< @
@  >>↑<>>>>↓      >↑ @
@↓<↓↑↓@@   >>>↓ >↓↑  @
@↑↑↑↑>@@      >>↑↑   @
@ ↓   @@             @
@@@@@@@@@@@@@@@@@@@@@@

Trajectory: 64 Steps

Max starting state return:
-184.11817393816816

epsilon: 0.54
gamma: 1.0

============= EPISODE 8000 =====

@@@@@@@@@@@@@@@@@@@@@@
@            @@  >>2 @
@            @@>>↑   @
@            @@↑     @
@            @@↑     @
@            >>↑     @
@    >↓     >↑       @
@  >>↑>>>>>>↑        @
@>>↑  @@             @
@↑    @@             @
@↓<   @@             @
@@@@@@@@@@@@@@@@@@@@@@

Trajectory: 31 Steps

Max starting state return:
-144.1876411414494

epsilon: 0.018541892629579577
gamma: 1.0

```


### What's missing
I haven't implemented importance sampling or various other discounting methods described in Chapter 5. 