package リポジトリー;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class VendingMachineGUI {
    // 既存のロジッククラスを部品として保持
    private final VendingMachineController vm;
    
    // GUIコンポーネント（画面の部品）
    private JFrame frame;
    private JLabel labelMoney;
    private JLabel labelSales;
    private JTextArea txtLog;
    private JPanel panelDrinks;

    public static void main(String[] args) {
        // GUIの起動処理（Swingの決まり文句）
        EventQueue.invokeLater(() -> {
            try {
                VendingMachineGUI window = new VendingMachineGUI();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public VendingMachineGUI() {
        // すでに作成済みのロジッククラスをインスタンス化
        this.vm = new VendingMachineController();
        initialize();
        updateDisplay();
    }

    /**
     * 画面のデザイン・レイアウトを初期化するメソッド
     */
    private void initialize() {
        // 1. メインウィンドウの設定
        frame = new JFrame();
        frame.setTitle("高機能自動販売機シミュレータ (GUI版)");
        frame.setBounds(100, 100, 650, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(10, 10));

        // 2. 上部ヘッダーパネル（ステータス表示）
        JPanel panelHeader = new JPanel();
        panelHeader.setLayout(new GridLayout(2, 1, 5, 5));
        panelHeader.setBorder(BorderFactory.createTitledBorder("ステータス"));
        
        labelMoney = new JLabel("投入金額: 0円");
        labelMoney.setFont(new Font("MS ゴシック", Font.BOLD, 16));
        labelSales = new JLabel("現在の総売上: 0円");
        labelSales.setFont(new Font("MS ゴシック", Font.PLAIN, 14));
        
        panelHeader.add(labelMoney);
        panelHeader.add(labelSales);
        frame.getContentPane().add(panelHeader, BorderLayout.NORTH);

        // 3. 中央パネル（商品ボタンの並び）
        panelDrinks = new JPanel();
        panelDrinks.setLayout(new GridLayout(1, 5, 10, 10)); // 横一列に5個並べる
        panelDrinks.setBorder(BorderFactory.createTitledBorder("商品メニュー（ボタンを押して購入）"));
        frame.getContentPane().add(panelDrinks, BorderLayout.CENTER);

        // 4. 下部パネル（操作・お釣り・ログ）
        JPanel panelFooter = new JPanel();
        panelFooter.setLayout(new BorderLayout(5, 5));
        
        // 操作ボタン群
        JPanel panelButtons = new JPanel();
        panelButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnInsert100 = new JButton("100円投入");
        JButton btnInsert500 = new JButton("500円投入");
        JButton btnInsert1000 = new JButton("1000円投入");
        JButton btnRefund = new JButton("お釣り（精算）");
        JButton btnAdmin = new JButton("管理者モード");
        
        panelButtons.add(btnInsert100);
        panelButtons.add(btnInsert500);
        panelButtons.add(btnInsert1000);
        panelButtons.add(btnRefund);
        panelButtons.add(btnAdmin);
        panelFooter.add(panelButtons, BorderLayout.NORTH);

        // コンソール代わりのログ表示エリア
        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setRows(6);
        JScrollPane scrollPane = new JScrollPane(txtLog);
        panelFooter.add(scrollPane, BorderLayout.CENTER);
        
        frame.getContentPane().add(panelFooter, BorderLayout.SOUTH);

        // --------------------------------------------------
        // イベントリスナー（ボタンが押された時の処理）
        // --------------------------------------------------
        btnInsert100.addActionListener(e -> handleInsert(100));
        btnInsert500.addActionListener(e -> handleInsert(500));
        btnInsert1000.addActionListener(e -> handleInsert(1000));
        
        btnRefund.addActionListener(e -> handleRefund());
        btnAdmin.addActionListener(e -> handleAdmin());
    }

    /**
     * 状態が変わるたびに画面の表示内容を最新にするメソッド
     */
    private void updateDisplay() {
        // 金額・売上の更新
        labelMoney.setText("投入金額: " + vm.getCurrentInsertedMoney() + "円 (上限: 1990円)");
        labelSales.setText("現在の総売上表示: " + vm.getTotalSales() + "円");

        // 商品ボタンエリアのリフレッシュ
        panelDrinks.removeAll();
        for (Drink d : vm.getDrinks()) {
            String btnText = "<html><center>" + d.getName() + "<br>" + d.getPrice() + "円<br>";
            btnText += d.isSoldOut() ? "<font color='red'>[売り切れ]</font>" : "[残り:" + d.getStock() + "個]";
            btnText += "</center></html>";

            JButton btnDrink = new JButton(btnText);
            
            // 購入可能かどうかに応じてボタンの有効/無効を切り替える
            if (d.isSoldOut() || vm.getCurrentInsertedMoney() < d.getPrice()) {
                btnDrink.setEnabled(false);
            }

            // 商品ボタンが押された時の処理
            btnDrink.addActionListener(e -> {
                String result = vm.purchaseDrink(d.getId());
                appendLog(result);
                updateDisplay(); // 購入完了後に画面を再描画
            });

            panelDrinks.add(btnDrink);
        }
        panelDrinks.revalidate();
        panelDrinks.repaint();
    }

    private void handleInsert(int amount) {
        String result = vm.insertMoney(amount);
        appendLog(result);
        updateDisplay();
    }

    private void handleRefund() {
        int totalChange = vm.getCurrentInsertedMoney();
        if (totalChange == 0) {
            appendLog("投入金額が0円のため、お釣りはありません。");
            return;
        }
        
        Map<Integer, Integer> details = vm.refund();
        StringBuilder sb = new StringBuilder();
        sb.append("お釣り合計: ").append(totalChange).append("円を返却しました。\n【内訳】 ");
        details.forEach((k, v) -> sb.append(k).append("円: ").append(v).append("枚 / "));
        
        appendLog(sb.toString());
        updateDisplay();
    }

    private void handleAdmin() {
        String inputPass = JOptionPane.showInputDialog(frame, "管理者パスワードを入力してください：", "管理者認証", JOptionPane.QUESTION_MESSAGE);
        if (inputPass == null) return; // キャンセルされた場合
        
        if (!"admin123".equals(inputPass)) {
            JOptionPane.showMessageDialog(frame, "エラー: パスワードが違います。", "認証失敗", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 管理者用の簡易ポップアップメニュー
        String[] options = {"売上金の回収", "全商品の補充(5個ずつ)", "金庫の残数確認", "閉じる"};
        int choice = JOptionPane.showOptionDialog(frame, "行う操作を選択してください。", "管理者メニュー",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (choice == 0) { // 回収
            int collected = vm.collectSales();
            appendLog("【管理者】売上金 " + collected + "円を回収しました。");
        } else if (choice == 1) { // 補充
            for (Drink d : vm.getDrinks()) {
                d.addStock(5);
            }
            appendLog("【管理者】すべての商品を5個ずつ補充しました。");
        } else if (choice == 2) { // 金庫確認
            StringBuilder sb = new StringBuilder("【釣銭金庫内 残数一覧】\n");
            vm.getCashVault().forEach((k, v) -> sb.append("  ").append(k).append("円: ").append(v).append("枚\n"));
            JOptionPane.showMessageDialog(frame, sb.toString(), "金庫確認", JOptionPane.INFORMATION_MESSAGE);
        }
        updateDisplay();
    }

    private void appendLog(String message) {
        txtLog.append(message + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength()); // 自動スクロール
    }
}