package com.ask.serialization.streams.output;

import com.ask.serialization.streams.test.entities.Animal;
import com.ask.serialization.streams.test.entities.Cat;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.Output;
import org.openjdk.jmh.annotations.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@Fork(value = 1)
public class WriteObjectBenchmarks {

    @State(Scope.Thread)
    public static class ObjectState {
        public Animal cat = new Cat(Cat.Color.BLACK, "Tom");
    }

    @Benchmark
    public void myOwn(ObjectState state) {
        OutputStream os = new MyOutputStream();
        os.writeObject(state.cat);
    }

    @Benchmark
    public void standardIO(ObjectState state) throws IOException {
        java.io.OutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(state.cat);
        oos.flush();
        oos.close();
    }

    @Benchmark
    public void kryoIO(ObjectState state) {
        Kryo kryo=new Kryo();
        java.io.OutputStream os = new ByteArrayOutputStream();
        Output bos = new ByteBufferOutput(os);
        kryo.writeObject(bos, state.cat);
        bos.close();
    }

//    @Benchmark
//    public void protobuf(ObjectState state) {
//    }
}
