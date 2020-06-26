package domain;

public class ConsumptionOverviewRecord {
    private int id;
    private double lastMonth, avgMonth, avgTrash;
    private String name;

    public ConsumptionOverviewRecord(int id, String name, double lastMonth, double avgMonth, double avgTrash) {
        this.id = id;
        this.name = name;
        this.lastMonth = lastMonth;
        this.avgMonth = avgMonth;
        this.avgTrash = avgTrash;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLastMonth() {
        return lastMonth;
    }

    public double getAvgMonth() {
        return avgMonth;
    }

    public double getAvgTrash() {
        return avgTrash;
    }

}
