A repo for an exercise from Sutton & Barto's Reinforcement Learning book (Chapter 5: Monte Carlo Methods)

Exercise 5.12 Racetrack
Driving a race car around a track.

This exercise helps illustrate the use of Monte Carlo methods in solving a reinforcement learning problems.
Through random trajectory sampling, we can derive a usable estimate for the value of actions for many states and
thus get a policy.

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

Have a look at the Sample Run for longer trace


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