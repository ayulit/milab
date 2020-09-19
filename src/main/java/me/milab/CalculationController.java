package me.milab;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.atomic.LongAdder;

@RestController
public class CalculationController {

    private LongAdder sum = new LongAdder();
    private DirectProcessor<String> processor = DirectProcessor.create();
    private FluxSink<String> sink = processor.sink();

    @PostMapping("/accumulate")
    public Mono<String> accumulate(@RequestBody String number) {
        return Mono.just(Long.parseLong(number))
                .publishOn(Schedulers.single())
                .doOnNext(sum::add)
                .flatMap(s -> Mono.from(processor));
    }

    @GetMapping("/calculate")
    public Mono<String> calculate() {
        return Mono
                .fromCallable(() -> Long.valueOf(sum.sumThenReset()).toString())
                .doOnNext(r -> sink.next(r));
    }
}
