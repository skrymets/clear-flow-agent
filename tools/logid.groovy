import java.time.*
import static java.time.temporal.ChronoUnit.SECONDS

def devStart = LocalDateTime.of(2017, Month.OCTOBER, 25, 17, 25, 00);

println String.format("__%08d", (SECONDS.between(devStart, LocalDateTime.now()) - 85000))
