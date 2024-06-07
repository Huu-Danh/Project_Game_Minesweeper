package com.mycompany.gamedomin;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

import javax.swing.*;

public class GameDoMin extends JFrame implements ActionListener, KeyListener {
    //so lan nhap

    ArrayList<GameRecord> gameRecords = new ArrayList<>();
    int countClicks = 0; // Đếm số lần click
    int BOM, dem = 0; // Số lượng bom và biến đếm bom
    int score = 0; // Điểm số
    int maxXY = 100; // Kích thước tối đa của bảng
    boolean die = false; // Trạng thái trò chơi (chết hay sống)
    Timer timer; // Bộ đếm thời gian
    boolean flag = false; // Trạng thái cờ (flag mode)
    private Color bom_cl = Color.red; // Màu bom
    int key_flag = KeyEvent.VK_H; // Phím tắt để bật/tắt flag
    private Color background_number_cl = Color.yellow; // Màu nền của ô có số
    private Color background_null_cl = Color.gray; // Màu nền của ô trống
    private Color flag_cl = Color.green; // Màu của cờ (flag)
    private int m, n; // Kích thước của bảng
    int M[] = {8, 15, 21}; // Các kích thước hàng của các mức độ
    int N[] = {10, 19, 27}; // Các kích thước cột của các mức độ
    int Mines[] = {10, 40, 100}; // Số lượng bom của các mức độ
    private int values[][] = new int[maxXY][maxXY]; // Ma trận giá trị của các ô
    private JButton bt[][] = new JButton[maxXY][maxXY]; // Ma trận các nút bấm
    private JLabel point_lb, hightPoint_lb, temp_lb, flag_lb, lv_lb; // Các label hiển thị
    private JComboBox lv = new JComboBox(); // ComboBox chọn mức độ
    private boolean tick[][] = new boolean[maxXY][maxXY]; //mảng 2 chièu chứa Trạng thái cờ của các ô
    private JButton repeat_bt, mines_bt, rank_bt; // Các nút chức năng
    private JPanel pn0, pn, pn2; // Các panel chứa thành phần giao diện
    private JLabel score_lb = new JLabel("Điểm số: 0"); // Label hiển thị điểm số
    Container cn; // Container chính của JFrame
    // Khoi tao voi tiêu đề và mức độ k

    public GameDoMin(String s, int k) {
        super(s);
        BOM = Mines[k];
        m = M[k];
        n = N[k];
        initBom();
        cn = this.getContentPane();
        //Panel chua cac nut dang luoi
        pn = new JPanel();
        // Thiết lập layout dạng lưới cho panel m dong va n cot
        pn.setLayout(new GridLayout(m, n));
        //Tạo bàn cờ với các button, set trạng thái cho ô là true (chưa check)
        for (int i = 0; i <= m + 1; i++) {
            for (int j = 0; j <= n + 1; j++) {
                bt[i][j] = new JButton("   ");
                tick[i][j] = true;
            }
        }
        //thêm các button vào panel chính, set sự kiện click và phím 
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                bt[i][j] = new JButton("   ");
                pn.add(bt[i][j]);
                bt[i][j].setActionCommand(i + " " + j);
                bt[i][j].addActionListener(this);
                bt[i][j].addKeyListener(this);
            }
        }
        //Khoi tao khung thoi gian
        point_lb = new JLabel("00:00:00:00");
        point_lb.setFont(new Font("Arial", 1, 20));
        hightPoint_lb = new JLabel(hightPoint(k));
        hightPoint_lb.setFont(new Font("Arial", 1, 20));
        temp_lb = new JLabel("     ");
        mines_bt = new JButton(String.valueOf(BOM));
        mines_bt.setBackground(flag_cl);
        repeat_bt = new JButton("Ván mới");
        repeat_bt.addActionListener(this);
        rank_bt = new JButton("Lịch sử thắng");
        rank_bt.addActionListener(this);
        flag_lb = new JLabel("Đang gỡ bom (h)");
        flag_lb.setForeground(bom_cl);
        flag_lb.setFont(new Font("Arial", 1, 15));
        lv_lb = new JLabel("Mức độ: ");
        lv_lb.setFont(new Font("Arial", 1, 15));
        //Combobox level 
        lv.addItem("Dễ");
        lv.addItem("Trung bình");
        lv.addItem("Khó");
        lv.setSelectedIndex(k);
        // Thêm các thành phần vào panel2 hiển thị các view header (mức độ, level 
        pn2 = new JPanel();
        pn2.setLayout(new FlowLayout());
        pn2.add(lv_lb);
        //cb level 
        pn2.add(lv);
        //so luong bom
        pn2.add(mines_bt);
        //nút ván mới 
        pn2.add(repeat_bt);
        //label trang thai "dang cam co" or "dang go bom"
        pn2.add(flag_lb);
        //nut hien thi danh sach rank 
        pn2.add(rank_bt);
        //Layout header (thanh tgian, diem so)
        pn0 = new JPanel();
        pn0.setLayout(new FlowLayout());
        //thanh tgian
        pn0.add(point_lb);
        pn0.add(temp_lb);
        //time out
        pn0.add(hightPoint_lb);
        //them panel 0 lên trên 
        cn.add(pn0, "North");
        cn.add(pn);
        //them panel 2 xuong duoi
        cn.add(pn2, "South");
        //diem so
        score_lb.setFont(new Font("Arial", 1, 15));
        pn0.add(score_lb);
        this.setVisible(true);
        this.pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        //cập nhật mỗi 10ms bằng cách sử dụng Swing Timer để lên lịch tác vụ  
        timer = new Timer(10, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                point_lb.setText(next(point_lb));

                if (next(point_lb).equals(hightPoint(k))) {
                    loss();
                    JOptionPane.showMessageDialog(null, "Thời gian kết thúc, bạn đã thua!");
                    timer.stop();
                    die = true;
                }
            }
        });
    }

    public void initBom() {
        float ratio = (float) 0.05; // Tỉ lệ tạo mìn những ô xung quanh
        int i, j;
        for (i = 0; i <= m + 1; i++) {
            for (j = 0; j <= n + 1; j++) {
                values[i][j] = 0;
            }
        }
        while (dem < BOM) {
            // Chọn ra tạo độ chưa phải là mìn.
            do {
                i = (int) ((m - 1) * Math.random()) + 1;
                j = (int) ((n - 2) * Math.random()) + 1;
            } while (values[i][j] != 0);
            if (values[i][j] == 0) {
                init(i, j, ratio);
            }
        }
    }

    public void init(int i, int j, float ratio) {
        float rand = (float) Math.random();
        if (rand < ratio) {
            values[i][j] = -1;
            for (int k = i - 1; k <= i + 1; k++) {
                for (int h = j - 1; h <= j + 1; h++) {
                    if (values[k][h] != -1) {
                        values[k][h]++;
                    }
                }
            }
            dem++;
            //Gọi hàm tạo bom cho những ô xung quanh.
            for (int k = i - 1; k <= i + 1; k++) {
                for (int h = j - 1; h <= j + 1; h++) {
                    if (k > 0 && h > 0 && values[k][h] != -1 && dem < BOM) {
                        init(k, h, ratio);
                    }
                }
            }
        }
    }

    public String next(JLabel lb) {
        String str[] = lb.getText().split(":");// Tách văn bản thành các thành phần
        int tt = Integer.parseInt(str[3]);// Chuyển đổi mili giây
        int s = Integer.parseInt(str[2]);// Chuyển đổi giây
        int m = Integer.parseInt(str[1]);// Chuyển đổi phút
        int h = Integer.parseInt(str[0]);// Chuyển đổi giờ
        String kq = "";// Khởi tạo chuỗi kết quả
        // Tính tổng thời gian tính bằng mili giây
        int sum = tt + s * 100 + m * 60 * 100 + h * 60 * 60 * 100 + 1;
        if (sum % 100 > 9) //kiểm tra ms nếu lớn hơn 9 thì thêm ms vào chuỗi kq
        {
            kq = ":" + sum % 100 + kq;
        } else {
            kq = ":0" + sum % 100 + kq;//nhỏ hơn 9 thêm 0 vào trước ms
        }                //vi du: 5ms thi kq = :05
        //lấy ra giay
        sum /= 100;
        if (sum % 60 > 9)//Nếu số s lớn hơn 9 thì thêm vào kq 
        {
            kq = ":" + sum % 60 + kq;
        } else {
            kq = ":0" + sum % 60 + kq;//nhỏ hơn thì thêm 0 vào trước 
        }                //vi du: 5s thi kq = :05:05
        //Lấy ra phut
        sum /= 60;
        if (sum % 60 > 9)//Kiểm tra số phut
        {
            kq = ":" + sum % 60 + kq;
        } else {
            kq = ":0" + sum % 60 + kq;//them 0 vao truoc
        }                //lay ra gio 
        sum /= 60;
        if (sum > 9) {
            kq = sum + kq;
        } else {
            kq = "0" + sum + kq;
        }
        return kq;
    }

    public void open(int i, int j) {//mở ô có số
        if (tick[i][j] && values[i][j] != -1) {
            bt[i][j].setText(String.valueOf(values[i][j]));
            bt[i][j].setBackground(background_number_cl);
            tick[i][j] = false;
            checkWin();
        }
    }

    public void openEmpty(int i, int j) {//mở ô rỗng
        if (tick[i][j]) {
            tick[i][j] = false;
            bt[i][j].setBackground(background_null_cl);
            checkWin();
            for (int h = i - 1; h <= i + 1; h++) {//loop chạy 9 ô xung quanh bt[i][j] và bản thân bt[i][j]
                for (int k = j - 1; k <= j + 1; k++) {
                    if (h >= 0 && h <= m && k >= 0 && k <= n) {
                        if (values[h][k] == 0 && tick[h][k]) {//nếu mở ra ô rỗng lại gọi hàm tiếp
                            openEmpty(h, k);
                        } else {
                            open(h, k);
                        }
                    }
                }
            }
        }
    }

    public void addFlag(int i, int j) {

        if (bt[i][j].getBackground() == flag_cl) {//nếu đã flag thì unflag
            tick[i][j] = true;//tắt flag cho ô
            bt[i][j].setBackground(null);
            bt[i][j].setIcon(null);
            mines_bt.setText(String.valueOf(Integer.parseInt(mines_bt.getText()) + 1));//chỉnh số mìn hiển thị
        } else if (tick[i][j] && Integer.parseInt(mines_bt.getText()) > 0) {//chưa flag thì flag
            tick[i][j] = false;//bật flag cho ô
            ImageIcon originalIcon = new ImageIcon("src/image/icons8-flag-48.png");
            Image originalImage = originalIcon.getImage();
            // Thay đổi kích thước hình ảnh để vừa với kích thước của nút
            Image scaledImage = originalImage.getScaledInstance(25, 25, Image.SCALE_SMOOTH);// kích thước icon
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            bt[i][j].setBackground(flag_cl);
            bt[i][j].setIcon(scaledIcon);
            bt[i][j].setText(null);  // Xóa văn bản
            bt[i][j].setHorizontalAlignment(SwingConstants.CENTER);
            bt[i][j].setVerticalAlignment(SwingConstants.CENTER);
            mines_bt.setText(String.valueOf(Integer.parseInt(mines_bt.getText()) - 1));//chỉnh số mìn hiển thị
        }
        checkWin();
    }

    public void checkWin() {
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (tick[i][j]) {//ít nhất 1 ô là true -> chưa mở hết ô -> return
                    return;
                }
            }
        }
        int k = 0;
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (bt[i][j].getBackground() == flag_cl && values[i][j] != -1)//ít nhất 1 ô đc đặt cờ với nền "flag_cl" và không phải mìn -> chưa kết thúc
                {
                    return;
                } else if (bt[i][j].getBackground() == flag_cl)//đc đánh dấu là cờ nhưng ko phải mìn -> tăng k 
                {
                    k++;
                }
            }
        }
        if (k <= BOM) {//Nếu số lượng cờ ít hoặc bằng số bom -> kết thúc trò chơi
            checkAndShowScore();
        }
    }

    String hightPoint(int k) {
        String file = "point.txt";
        String str = "";
        FileReader fr;
        try {
            fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            try {
                for (int i = 0; i <= k; i++) {
                    str = br.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            String maxTime = "99:99:99:99";
            FileWriter f;
            try {
                f = new FileWriter(file);
                f.write(maxTime + "\n");
                f.write(maxTime + "\n");
                f.write(maxTime + "\n");
                f.flush();
                f.close();
                return "99:99:99:99";
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return str;
    }

    public void loss() {//chạy vòng lặp để hiển thị các ô chứa bom còn lại
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (values[i][j] == -1) {
                    ImageIcon originalIcon = new ImageIcon("src/image/icons8-boom-64.png");
                    Image originalImage = originalIcon.getImage();
                    // Thay đổi kích thước hình ảnh để vừa với kích thước của nút
                    Image scaledImage = originalImage.getScaledInstance(25, 25, Image.SCALE_SMOOTH);
                    ImageIcon scaledIcon = new ImageIcon(scaledImage);
                    bt[i][j].setBackground(bom_cl);
                    bt[i][j].setIcon(scaledIcon);
                    bt[i][j].setText(null);  // Xóa văn bản
                    bt[i][j].setHorizontalAlignment(SwingConstants.CENTER);
                    bt[i][j].setVerticalAlignment(SwingConstants.CENTER);
                }
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(repeat_bt.getText())) {
            checkAndShowScore(); // Kiểm tra điểm trước khi tạo mới trò chơi
            new GameDoMin("Nhóm 2 - Game Dò Mìn", lv.getSelectedIndex()/*lấy độ khó người chơi chọn từ combobox*/);
            this.dispose();
        } else if (e.getActionCommand().equals(rank_bt.getText())) {
            showRank();
        } else if (!die) {
            timer.start();
            int i = 0, j = 0;
            String s = e.getActionCommand();
            System.out.println("s = " + s);//lấy tọa độ i, j
            int k = s.indexOf(32);
            System.out.println("k = " + k);//lấy index spacebar
            i = Integer.parseInt(s.substring(0, k));
            j = Integer.parseInt(s.substring(k + 1, s.length()));
            if (tick[i][j]/*chưa flag*/ && !flag/*không trong chế độ flag*/) {
                this.countClicks++;
                if (values[i][j] == -1) {
                    if (bt[i][j].getBackground() != flag_cl) {
                        timer.stop();
                        loss();
                        JOptionPane.showMessageDialog(null, "Trúng Bom, bạn đã thua!");
                        die = true;
                    }
                } else if (values[i][j] == 0) {//ô rỗng thì mở những ô rỗng kế
                    System.out.println(values[i][j]);
                    openEmpty(i, j);
                } else {//ô có số thì mở mỗi ô đó thui
                    System.out.println(values[i][j]);
                    open(i, j);
                }
                double efficiencyIndex = calculateEfficiencyIndex(countClicks);
                score = calculateScore(countClicks, efficiencyIndex);
                score_lb.setText("Điểm: " + String.valueOf(score));
                score_lb.setFont(new Font("Arial", 1, 15));
                pn0.add(score_lb);
            } else if (flag) {
                addFlag(i, j);
            }

        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == key_flag) {
            flag = !flag;
        }
        if (flag) {
            flag_lb.setText("Đang cắm cờ (h)");
            flag_lb.setForeground(flag_cl);
        } else {
            flag_lb.setText("Đang gỡ bom (h)");
            flag_lb.setForeground(bom_cl);
        }

    }

    public void checkAndShowScore() {
        if (!die) {
            int clicks = countClicks;
            double efficiencyIndex = calculateEfficiencyIndex(clicks);
            score = calculateScore(clicks, efficiencyIndex);
            timer.stop();
            JOptionPane.showMessageDialog(this, "Chúc mừng! Bạn đã hoàn thành trò chơi với số điểm: " + score);
            score_lb.setText("Điểm số: " + score);
            saveGameHistory();
            die = true;
        }
    }

    // Phuong thuc tinh hieu xuat
    public double calculateEfficiencyIndex(int clicks) {
        return (BOM * 100.0) / (clicks + BOM);
    }

    public int calculateScore(int clicks, double efficiencyIndex) {
        // Điểm = Số lần nhấp chuột * (100 - Chỉ số hiệu suất)
        return (int) (clicks * (100.0 - efficiencyIndex));
    }

    private void showRank() {
        gameRecords = readGameHistory();
        gameRecords.sort(Comparator.comparing(GameRecord::getScore).reversed());
        //check du lieu
        if (!gameRecords.isEmpty()) {
            // Hiển thị dữ liệu xếp hạng
            StringBuilder rankInfo = new StringBuilder();
            int rank = 1;
            for (GameRecord record : gameRecords) {
                rankInfo.append("Top").append(rank).append(": Diem: ").append(record.getScore()).append(", Thoi gian: ").append(record.getDate()).append(" ").append(record.getTime()).append("\n");
                rank++;
            }
            JOptionPane.showMessageDialog(this, "Rank:\n" + rankInfo.toString(), "Rank", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu Rank!", "Rank", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void saveGameHistory() {
        String fileName = "game_history.txt";
        try {
            FileWriter writer = new FileWriter(fileName, true); // Append mode
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write("Game History:\n");
            bufferedWriter.write("Date: " + java.time.LocalDate.now() + "\n");
            bufferedWriter.write("Time: " + java.time.LocalTime.now() + "\n");
            bufferedWriter.write("Number of clicks:" + this.countClicks + "\n");
            bufferedWriter.write("Score: " + score + "\n");
            bufferedWriter.write("------------------------------------\n");
            // Write additional game history information as needed
            bufferedWriter.close();
            System.out.println("Game history saved successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Game history saved faile! \n");
        }
    }

    private ArrayList<GameRecord> readGameHistory() {
        String fileName = "game_history.txt";
        ArrayList<GameRecord> gameRecords = new ArrayList<>();

        try {
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            String line;

            while ((line = br.readLine()) != null) {
                if (line.startsWith("Date: ")) {
                    String[] parts = line.split(":");
                    String date = parts[1].trim(); //lay phan tu dau
                    String time = br.readLine().split(":")[1].trim();
                    long clicks = Long.parseLong(br.readLine().split(":")[1].trim());
                    int score = Integer.parseInt(br.readLine().split(":")[1].trim());
                    br.readLine();
                    gameRecords.add(new GameRecord(date, time, clicks, score));
                }
            }

            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load rank!", "Error", JOptionPane.ERROR_MESSAGE);
        }

        return gameRecords;
    }

    public void keyTyped(KeyEvent e) {

    }

    public void keyReleased(KeyEvent e) {

    }

    public static void main(String[] args) {
        new GameDoMin("Nhóm 2 - Game Dò Mìn", 0);
    }
}
