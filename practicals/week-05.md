# Week 5: branch coverage and MC/DC

So far you have chosen tests from the rules: the partitions and boundaries of
the fares, the behaviour each seam had to keep. This week you look at the same
tests from the other side, from the code. Which branches in `domain` have your
tests taken, and which have they never taken? And where a decision rests on
more than one condition, has every condition been shown to matter?

## What arrives this week

| File | What it is |
|---|---|
| `practicals/week-05.md` | This sheet |

Nothing else: everything this week uses is already in your repository. JaCoCo
has written a coverage report on every `mvn verify` since week 1, and
`CancellationPolicy` has been in `domain` from the start.

```bash
git pull
mvn verify
```

## Reading the report

Open `target/site/jacoco/index.html` in a browser. It lists every package;
follow `mt.edu.um.cps3230.ferry.domain`, then a class, then a method, to reach
its source, coloured line by line:

| Colour | Means |
|---|---|
| green | every instruction on the line ran |
| yellow | the line ran, but not every branch on it was taken |
| red | the line never ran |

A diamond in the margin marks a line with branches. Hover over it to read, for
example, `1 of 4 branches missed`. The tables on the package and class pages
count instructions, branches, lines and methods; `Cxty` is the cyclomatic
complexity of each method.

JaCoCo counts branches in the compiled code, not in the source. Each condition
is compiled to a jump of its own, so this line from `BookingRequest` has four
branches, each of its two conditions once true and once false:

```java
if (contactEmail == null || !contactEmail.contains("@")) {
```

Full branch coverage in JaCoCo therefore means that every condition has come
out both ways, which asks for more than every `if` going both ways. Task 3
shows that it is still less than MC/DC.

## Where your tests go

Put them in `src/test/java/mt/edu/um/cps3230/ferry/domain/`, in as many
classes as you like, next to the ones already there. `Fixtures` builds
sailings, passengers and bookings. The stand-ins from week 4 are what the
`domain` classes need: `FixedWeatherFeed`, `InMemoryBookingRepository`,
`InMemorySailingRepository`, `FakePaymentGateway`,
`InMemoryNotificationGateway` and a fixed clock (`Fixtures.clockAt`). Name
each test `should...When...`.

## Tasks

### 1. Where the gaps are

Run `mvn verify` and open the report for `domain`. With only the tests the
repository started with, about a third of its branches are taken; the tests
you wrote in earlier weeks will have raised that. For every class with a
branch missed, write down the lines, and for each line the rule it belongs to.
The Javadoc in each class and the sheets for weeks 3 and 4 state the rules.
Then sort the gaps:

- a rule that no test checks yet;
- a refusal: something the code turns down with an exception;
- a branch you think is not worth a test, with your reason.

Keep the list: tasks 4 and 5 come back to it.

### 2. MC/DC for `CancellationPolicy.decide`

```java
Forecast forecast = weather.forecastAt(sailing.departsAt());
if (forecast.waveHeightMetres() >= cancelWaveHeightMetres
        || forecast.windGustKph() >= cancelGustKph) {
    return SailingStatus.CANCELLED;
}
if (forecast.waveHeightMetres() >= atRiskWaveHeightMetres
        || forecast.windGustKph() >= atRiskGustKph) {
    return SailingStatus.AT_RISK;
}
return SailingStatus.SAILS;
```

There are two decisions here, each made of two conditions. With the
operator's thresholds:

| Condition | True when | Decision |
|---|---|---|
| A | the waves are at least 2.5 m | first, `A \|\| B`: the sailing is cancelled |
| B | the gusts are at least 60 km/h | first |
| C | the waves are at least 1.8 m | second, `C \|\| D`: the sailing is at risk |
| D | the gusts are at least 45 km/h | second |

Modified condition/decision coverage (MC/DC) asks for every condition to be
shown to change the outcome of its decision on its own. For each condition you
need an **independence pair**: two tests in which that condition differs, every
other condition of the same decision stays the same, and the decision comes
out differently.

1. Write the truth table of `A || B`. Which pairs of rows are independence
   pairs for A? Which for B? What is the smallest set of rows that holds a
   pair for both?
2. Do the same for `C || D`. The second decision is reached only when the
   first came out false: what must A and B be in every row you use?
3. Turn the rows into forecasts, a wave height and a gust, each with the
   status it must give. Five tests are enough for both decisions together.
   Find five, and say for each condition which two of them form its pair.
4. Write them as one parameterised test (`@ParameterizedTest` with
   `@CsvSource`), one row per test, with a comment that lists the pairs.
   `FixedWeatherFeed` hands the policy whatever forecast a row asks for:

```java
CancellationPolicy policy = new CancellationPolicy(
        new FixedWeatherFeed(waveHeightMetres, windGustKph),
        new InMemoryBookingRepository(), new FakePaymentGateway(),
        new InMemoryNotificationGateway(), new PriceCalculator());
```

Then think about these:

- Can the waves be at least 2.5 m without being at least 1.8 m? Conditions
  that read the same input are *coupled*, and some rows of the full truth
  table cannot happen. Did that stop you finding a pair?
- Java does not evaluate B at all when A is true. Does that change which rows
  you need?
- An MC/DC set does not have to sit on the thresholds. Would yours notice if
  one `>=` were written `>`? If not, move the row that makes that condition
  true onto the threshold itself. One set of tests can then serve MC/DC and
  the boundaries of week 3 together.

### 3. Branch coverage is not MC/DC

With your five tests in, the report shows all 8 branches of `decide` taken.
These five forecasts take all 8 as well:

| Waves (m) | Gusts (km/h) | Status |
|---|---|---|
| 3.0 | 70 | `CANCELLED` |
| 1.0 | 70 | `CANCELLED` |
| 2.0 | 50 | `AT_RISK` |
| 1.0 | 50 | `AT_RISK` |
| 1.0 | 20 | `SAILS` |

Check that claim against your truth tables: which branches does each row
take? Then:

- Which conditions have no independence pair in this set?
- Suppose a mistake left `decide` reading only the gusts, with both wave
  conditions gone. Work out by hand what each of the five rows would then
  return. Which rows would notice? Which of your own five tests would?
- What does a report showing every branch of a method taken tell you, and
  what does it not?

### 4. Ninety per cent of the branches in `domain`

Write tests until the report shows at least 90% of the branches in `domain`
taken. Work from your list in task 1, rule first: for each branch, write a
test of the rule it belongs to, with that branch's outcome as the expected
result. A test that runs a line without checking what it did raises the
number and tests nothing.

Some places to start:

- `BookingService` and `CancellationPolicy` hold the most branches. In
  `BookingService` they are rules with a time or a capacity in them: a sailing
  that has left, a sailing with no places left, a reservation held too long, a
  card turned down, a cancellation too close to departure. Give the service a
  fixed clock and a sailing small enough to fill.
- `CancellationPolicy.apply` acts on the decision. On each of the three
  statuses, what happens to a reservation, to a booking already paid for, to
  a booking already called off, and to their contacts?
  `InMemoryBookingRepository`, `FakePaymentGateway` and
  `InMemoryNotificationGateway` let a test read back what happened.
- `Booking.hasExpired` and `Booking.holdsPlacesAt` are compound conditions
  too, joined with `&&`. Give each the treatment from task 2: three tests
  apiece.
- Every refusal in the records (`Money`, `Sailing`, `SailingAvailability`,
  `TimetableEntry`, `Passenger`, `Vehicle`, `BookingRequest`, `Booking`) is a
  branch. Some of them you tested in week 3.

### 5. Look back

- Which branches from your list in task 1 did you leave untested? Does the 90%
  bar leave room for that choice, and would 100% have been worth the tests it
  takes?
- Pick one of your tests and delete its assertions, keeping the call. What
  happens to the coverage? What does that tell you about what coverage
  measures?
- Compare each method's `Cxty` with the number of tests you needed to take
  all of its branches.

## Self check

```bash
mvn verify -Pcheck -Dweek=05
```

The self check runs all of your tests, then holds the `domain` package to 90%
of its branches taken. Every test has to pass first: a failing test stops the
build before coverage is counted. Below 90%, the build fails with a line like
this one, with your own figure in it:

```
[WARNING] Rule violated for package mt.edu.um.cps3230.ferry.domain: branches covered ratio is 0.34, but expected minimum is 0.90
```

A green self check says that your tests take enough branches. It cannot say
whether they check anything once they get there, and it does not look for
MC/DC at all: JaCoCo counts branches, and task 3 shows why that is not the
same thing. What your tests check is what you measure in week 6.
