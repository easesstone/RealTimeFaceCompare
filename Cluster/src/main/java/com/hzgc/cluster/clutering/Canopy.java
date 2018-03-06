package com.hzgc.cluster.clutering;


import java.util.ArrayList;
import java.util.List;


/**
 * Canopy算法 借助canopy算法计算对应的Kmeans中的K值大小
 * 其中对于计算K值来说，canopy算法中的T1没有意义，只用设定T2(T1>T2) 我们这里将T2设置为平均距离
 *
 * @author YD
 */
public class Canopy {
    private List<Point> points = new ArrayList<Point>(); // 进行聚类的点
    private List<List<Point>> clusters = new ArrayList<List<Point>>(); // 存储簇
    private double T2 = -1; // 阈值

    public Canopy(List<Point> points) {
        for (Point point : points) {
            // 进行深拷贝
            this.points.add(point);
        }
    }

    public void cluster() {
        T2 = getAverageDistance(points);
        while (points.size() != 0) {
            List<Point> cluster = new ArrayList<Point>();
            Point basePoint = points.get(0); // 基准点
            cluster.add(basePoint);
            points.remove(0);
            int index = 0;
            while (index < points.size()) {
                Point anotherPoint = points.get(index);
                double distance = Math.sqrt((basePoint.getX() - anotherPoint.getX())
                        * (basePoint.getX() - anotherPoint.getX())
                        + (basePoint.getY() - anotherPoint.getY())
                        * (basePoint.getY() - anotherPoint.getY()));
                if (distance <= T2) {
                    cluster.add(anotherPoint);
                    points.remove(index);
                } else {
                    index++;
                }
            }
            clusters.add(cluster);
        }
    }


    public int getClusterNumber() {
        return clusters.size();
    }


    public List<Point> getClusterCenterPoints() {
        List<Point> centerPoints = new ArrayList<Point>();
        for (List<Point> cluster : clusters) {
            centerPoints.add(getCenterPoint(cluster));
        }
        return centerPoints;
    }


    private double getAverageDistance(List<Point> points) {
        double sum = 0;
        int pointSize = points.size();
        for (int i = 0; i < pointSize; i++) {
            for (int j = 0; j < pointSize; j++) {
                if (i == j) {
                    continue;
                }
                Point pointA = points.get(i);
                Point pointB = points.get(j);
                sum += Math.sqrt((pointA.getX() - pointB.getX()) * (pointA.getX() - pointB.getX())
                        + (pointA.getY() - pointB.getY()) * (pointA.getY() - pointB.getY()));
            }
        }
        int distanceNumber = pointSize * (pointSize + 1) / 2;
        double T2 = sum / distanceNumber / 2; // 平均距离的一半
        return T2;
    }

    private Point getCenterPoint(List<Point> points) {
        double sumX = 0;
        double sumY = 0;
        for (Point point : points) {
            sumX += point.getX();
            sumY += point.getY();
        }
        int clusterSize = points.size();
        Point centerPoint = new Point(sumX / clusterSize, sumY / clusterSize);
        return centerPoint;
    }

    public double getThreshold() {
        return T2;
    }


    public static void main(String[] args) {
        List<Point> points = new ArrayList<Point>();
        points.add(new Point(0, 0));
        points.add(new Point(0, 1));
        points.add(new Point(1, 0));

        points.add(new Point(5, 5));
        points.add(new Point(5, 6));
        points.add(new Point(6, 5));

        points.add(new Point(10, 2));
        points.add(new Point(10, 3));
        points.add(new Point(11, 3));

        Canopy canopy = new Canopy(points);
        canopy.cluster();

        //获取canopy数目
        int clusterNumber = canopy.getClusterNumber();
        System.out.println(clusterNumber);

        //获取canopy中T2的值
        System.out.println(canopy.getThreshold());
    }
}