package com.cloud_guest.vo;

import com.benjaminwan.ocrlibrary.Point;
import com.benjaminwan.ocrlibrary.TextBlock;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

/**
 * @Author yan
 * @Date 2025/9/22 14:41:51
 * @Description
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextBlockVo {
    private ArrayList<Point> boxPoint;
    private float boxScore;
    private int angleIndex;
    private float angleScore;
    private double angleTime;
    private String text;
    private float[] charScores;
    private double crnnTime;
    private double blockTime;
    private int x;
    private int y;
    private int width;
    private int height;

    public TextBlockVo(TextBlock textBlock) {
        this.boxPoint = textBlock.getBoxPoint();
        this.boxScore = textBlock.getBoxScore();
        this.angleIndex = textBlock.getAngleIndex();
        this.angleScore = textBlock.getAngleScore();
        this.angleTime = textBlock.getAngleTime();
        this.text = textBlock.getText();
        this.charScores = textBlock.getCharScores();
        this.crnnTime = textBlock.getCrnnTime();

        this.boxPoint.forEach(point -> {
            this.x = Math.min(this.x, point.getX());
            this.y = Math.min(this.y, point.getY());
        });

        this.width = Math.abs(this.boxPoint.get(0).getX() - this.boxPoint.get(1).getX());
        this.height = Math.abs(this.boxPoint.get(0).getY() - this.boxPoint.get(1).getY());
    }
}
