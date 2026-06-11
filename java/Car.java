public class Car {
    private CarType        carType;
    private Engine         engine;
    private BrakeSystem    brakeSystem;
    private SteeringSystem steeringSystem;

    public CarType        getCarType()        { return carType; }
    public Engine         getEngine()         { return engine; }
    public BrakeSystem    getBrakeSystem()    { return brakeSystem; }
    public SteeringSystem getSteeringSystem() { return steeringSystem; }

    public void setCarType       (CarType t)        { this.carType        = t; }
    public void setEngine        (Engine e)         { this.engine         = e; }
    public void setBrakeSystem   (BrakeSystem b)    { this.brakeSystem    = b; }
    public void setSteeringSystem(SteeringSystem s) { this.steeringSystem = s; }
}
