package com.mycompany.gamedomin;

/**
 *
 * @author tungs
 */
class GameRecord {
    private String date;
    private String time;
    private long clicks;
    private int score;

    public GameRecord(String date, String time, long clicks, int score) {
        this.date = date;
        this.time = time;
        this.clicks = clicks;
        this.score = score;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public long getClicks() {
        return clicks;
    }

    public int getScore() {
        return score;
    }

    @Override
    public String toString() {
        return "Game History:\n" +
                "Date: " + date + "\n" +
                "Time: " + time + "\n" +
                "Number of clicks: " + clicks + "\n" +
                "Score: " + score + "\n" +
                "------------------------------------";
    }
}
