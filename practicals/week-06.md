# Week 6: mutation testing and test smells

Week 5 ended on a warning: a test that runs a line without checking what it
did still counts as coverage. This week you measure what coverage cannot,
whether your tests would notice if the code were wrong. A mutation testing
tool makes many small changes to the code, one at a time, runs your tests
against each, and reports every change that no test noticed.

The second half of the week is about the tests themselves. A test class that
passes can still be hard to read, fragile and slow. You get one such class,
find what is wrong with it, and refactor it, with mutation testing as your
check that the refactored tests still catch what the old ones caught.

## What arrives this week

| File | What it is |
|---|---|
| `src/test/java/mt/edu/um/cps3230/ferry/smells/LegacyBookingServiceTest.java` | Tests for `BookingService` kept from an earlier version of the application. They all pass |
| `practicals/week-06.md` | This sheet |

PIT, the mutation testing tool, has been on the build since week 1.

```bash
git pull
mvn verify
```

## Running PIT

```bash
mvn test-compile pitest:mutationCoverage
```

PIT mutates the compiled classes, so the code has to be built first, which is
what `test-compile` does. It mutates `domain` and `kata`, runs your tests
against each mutant, and writes its report to `target/pit-reports/index.html`.

Each change PIT makes is a **mutant**. For each one, it reports:

| Status | Means |
|---|---|
| `KILLED` | at least one test failed against the mutant: your tests noticed |
| `SURVIVED` | every test that runs the mutated line still passed |
| `NO_COVERAGE` | no test runs the mutated line at all |
| `TIMED_OUT` | the tests ran far longer than usual, usually a loop that no longer ends; PIT counts it as killed |

The report gives three figures per package and class. *Line coverage* is the
lines your tests run. *Mutation coverage* is the share of all mutants killed,
which is the figure this week's self check uses. *Test strength* is the share
of the mutants on lines your tests run that they killed. Follow a class to its
source: each line lists the mutants made on it and what happened to each.

PIT's default changes include:

| The report says | The change |
|---|---|
| changed conditional boundary | `<` becomes `<=`, `>=` becomes `>`, and so on |
| negated conditional | `==` becomes `!=`, `<` becomes `>=`, and so on |
| removed call to ... | a call to a `void` method is deleted |
| replaced return value with ... | the method returns `null`, `""`, `0`, `true`, `false` or an empty list instead |
| Replaced ... subtraction with addition | an arithmetic operator is swapped |

For a test to kill a mutant, three things have to happen: the test **reaches**
the mutated line; the mutant changes some value there (it **infects** the
state); and that difference **propagates** to something the test asserts on.
A surviving mutant has failed at one of the three.

## Where your tests go

Tests for `domain` go in `src/test/java/mt/edu/um/cps3230/ferry/domain/`, as
in weeks 3 and 5, and may use `Fixtures`. The refactored smells class stays
where it is, in `smells/`. `Fixtures` is not visible from that package, so
anything it needs, it builds itself. Name each test `should...When...`.

## Tasks

### 1. Reading the report

Run PIT and open the report for `domain`.

1. Write down the mutation coverage and the line coverage of each class. Which
   class has the widest gap between the two?
2. Pick three mutants that `SURVIVED`, from different classes. For each, write
   down the line, the change, and which of reach, infect and propagate failed.
3. Find a line that JaCoCo shows green (`target/site/jacoco/index.html`, from
   `mvn verify`) and on which a mutant survives. What does each tool say about
   that line?
4. Look at the mutants PIT made in `CancellationPolicy.decide`, two on each
   of its four conditions. With only the tests the repository started with,
   one on each condition survives: which? Which week 3 technique kills them?
   If you put week 5's MC/DC rows on the thresholds, they are killed already.

### 2. Killing mutants

Write tests until no mutant you can kill in `BookingService`, `Booking`,
`Money` and `CancellationPolicy` survives. As in week 5, work rule first: for
each mutant, find the rule it breaks, and write a test of that rule. Name the
test after the rule, never after the mutant.

Some places to start:

- `Money`: every range has two edges. Which edges have a test on each side?
- `BookingService`: a capacity check that never sees a vehicle, or a
  reservation that has run out, cannot notice a mutant that only matters then.
  Use a fixed clock to move past `Booking.RESERVATION_WINDOW`.
- `CancellationPolicy.apply`: which mutants are `NO_COVERAGE`? What would a
  test need to read back to notice a deleted `refund`, `save` or `send`?

Delete the assertions from one of your new tests, keeping the call, and run
PIT again. Compare what happens to line coverage and to mutation coverage.

### 3. Mutants no test can kill

A mutant that changes the code without changing what it does is
**equivalent**: no test can kill it, and it holds the score down for no
reason. Look at `SailingAvailability`:

```java
public int passengerPlacesLeft() {
    return Math.max(0, sailing.passengerCapacity() - passengersBooked);
}

public boolean isFull() {
    return passengerPlacesLeft() == 0;
}
```

- Suppose a mutant changed `== 0` in `isFull` to `<= 0`. Can any test kill
  it? Explain why, from the code.
- Suppose instead a mutant removed the `Math.max`, leaving
  `sailing.passengerCapacity() - passengersBooked`. Can a test kill that one?
  If so, write it.
- When PIT reports a survivor that you believe is equivalent, what should you
  do with it, and what should you write down?

### 4. Smells in a test class

Read `smells/LegacyBookingServiceTest`. Every test in it passes, and between
them they carry five test smells:

| Smell | What it is |
|---|---|
| Assertion roulette | many assertions in one test, none saying what it checks, so a failure does not tell you which behaviour broke |
| Eager test | one test that checks several behaviours of the code under test |
| Mystery guest | the test depends on something outside it, such as a file, that the reader cannot see |
| Conditional test logic | an `if` or a loop in a test, so what the test checks depends on how it runs |
| Sleeping test | `Thread.sleep`, waiting for time to pass instead of controlling it |

For each smell, quote the lines that carry it (some tests carry more than
one) and answer the question that goes with it:

- **Assertion roulette.** Change the `1045` in the first test to `1050` and
  run the class. From the failure report alone, without the source, which
  behaviour broke?
- **Eager test.** A mistake that broke check-in would fail which test? What
  would its name tell you?
- **Mystery guest.** What happens to these tests if the operator adds a 06:30
  sailing from Ċirkewwa to `sailings.csv`? If it lowers the capacity of the
  07:00 sailing to 800?
- **Conditional test logic.** Which ages does the loop actually try? Does it
  try both sides of the infant boundary and of the child boundary? If
  `PriceCalculator` and this test had made the same mistake about a fare,
  would the test notice?
- **Sleeping test.** Why is the `Thread.sleep` there? Remove it and run the
  class several times. What happens, and why not every time? The service is
  given its references and its clock through its constructor, as in week 4:
  what should this test give it instead?

Note anything else you would change, and say why.

### 5. Refactoring the smells away

Refactoring a test changes how it checks, never what it checks. PIT tells you
whether that held.

1. Before you change anything, run PIT and copy `target/pit-reports` to a
   folder outside the repository. This is your record of what the tests
   killed.
2. Refactor `LegacyBookingServiceTest` until none of the five smells is left:
   one behaviour per test, nothing read from `sailings.csv`, no
   `Thread.sleep`, no system clock, no `if` or loop. A parameterised test can
   take the place of a loop.
3. Run PIT again and compare the two reports for `BookingService`. Every
   mutant in it that was killed before must still be killed. If one is not,
   your refactoring dropped a check: find which, and put it back. An eager
   test checks some things only in passing, and those are the easiest to lose.
4. Elsewhere in `domain` a few mutants may now survive that were killed
   before, in classes the old tests never meant to test. Which ones, and why
   were they killed before? Whose job is it to test those classes?

### 6. Your kata

PIT mutates the `kata` package too. Look at its report for your
`StringCalculator` from week 2.

- Which mutants survive? What does each say about the tests you wrote while
  doing TDD?
- Test driven development writes a failing test before every change. Should
  that leave any mutant alive? Why might it still?

### 7. Look back

- Compare your mutation coverage for `domain` with your branch coverage from
  week 5. Which is lower, and what does the gap consist of?
- For every mutant, PIT runs the tests that reach its line until one of them
  fails. What did the `Thread.sleep` in the old smells class cost each PIT
  run?
- Would 100% mutation coverage be worth the tests it takes? What would you
  set the bar at for code you were paid to maintain, and why?

## Self check

```bash
mvn verify -Pcheck -Dweek=06
```

The self check runs all of your tests, then runs PIT on the `domain` package
and holds it to 75% mutation coverage. Every test has to pass first: a failing
test stops the build before PIT runs. `kata` is not counted. Below 75%, the
build fails with a line like this one, with your own figure in it:

```
[ERROR] Failed to execute goal org.pitest:pitest-maven:1.30.0:mutationCoverage (check-domain-mutations) on project ferry: Mutation score of 60 is below threshold of 75 -> [Help 1]
```

A green self check says that your tests notice most small changes to
`domain`. It says nothing about whether they are pleasant to read: the smells
class passes it as readily as your refactored version does. Task 4 and task 5
are the part of this week that no tool checks.
