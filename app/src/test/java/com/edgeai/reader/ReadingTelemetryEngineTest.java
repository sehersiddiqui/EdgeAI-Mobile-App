package com.edgeai.reader;
import org.junit.Test;
import static org.junit.Assert.*;

public class ReadingTelemetryEngineTest {
    @Test public void fixationTriggersAfterThreshold() {
        ReadingTelemetryEngine engine=new ReadingTelemetryEngine();
        engine.accept(new ReadingTelemetryEngine.Sample(.4f,.5f,1000));
        ReadingTelemetryEngine.Event event=engine.accept(new ReadingTelemetryEngine.Sample(.405f,.505f,1701));
        assertTrue(event.fixationAlert);
        assertTrue(event.fixationDurationMs>600);
    }
    @Test public void leftwardJumpsTriggerAfterMoreThanThree() {
        ReadingTelemetryEngine engine=new ReadingTelemetryEngine();
        long t=1000;
        engine.accept(new ReadingTelemetryEngine.Sample(.8f,.4f,t));
        boolean triggered=false;
        for(int i=0;i<4;i++){
            ReadingTelemetryEngine.Event e=engine.accept(new ReadingTelemetryEngine.Sample(.2f,.42f,t+=100));
            triggered |= e.regressionAlert;
            engine.accept(new ReadingTelemetryEngine.Sample(.8f,.42f,t+=100));
        }
        assertTrue(triggered);
    }
    @Test public void normalizedCoordinatesAreClamped() {
        ReadingTelemetryEngine.Sample s=new ReadingTelemetryEngine.Sample(1.7f,-.2f,0);
        assertEquals(1f,s.x,0f); assertEquals(0f,s.y,0f);
    }
}
