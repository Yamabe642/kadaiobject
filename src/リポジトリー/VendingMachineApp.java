package リポジトリー;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

// ==========================================
// 1. 商品情報を管理するクラス
// ==========================================
class Drink {
    private final int id;
    private final String name;
    private final int price;
    private int stock;

    public Drink(int id, String name, int price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getStock() { return stock; }

    public void decreaseStock() {
        if (this.stock > 0) this.stock--;
    }

    public boolean isSoldOut() {
        return this.stock <= 0;
    }

    public void addStock(int amount) {
        if (amount > 0) this.stock += amount;
    }
}

// ==========================================
// 2. 自動販売機のロジック・金庫・ログ管理クラス
// ==========================================
class VendingMachineController {
    private final List<Drink> drinks;
    private int currentInsertedMoney;
    private int totalSales;
    private final Map<Integer, Integer> cashVault;
    
    private static final int MAX_MONEY = 1990;
    private static final String LOG_FILE = "vending_machine_log.csv";

    public VendingMachineController() {
        this.drinks = new ArrayList<>();
        this.cashVault = new LinkedHashMap<>();
        this.currentInsertedMoney = 0;
        this.totalSales = 0;
        
        initializeDrinks();
        initializeVault();
    }

    private void initializeDrinks() {
        drinks.add(new Drink(1, "缶コーヒー", 110, 5));
        drinks.add(new Drink(2, "缶お茶", 120, 5));
        drinks.add(new Drink(3, "スポーツドリンク", 130, 3));
        drinks.add(new Drink(4, "炭酸ソーダ", 150, 2));
        drinks.add(new Drink(5, "果汁100%ジュース", 160, 4));
    }

    private void initializeVault() {
        cashVault.put(1000, 10);
        cashVault.put(500, 10);
        cashVault.put(100, 10);
        cashVault.put(50, 10);
        cashVault.put(10, 10);
    }

    public List<Drink> getDrinks() { return drinks; }
    public int getCurrentInsertedMoney() { return currentInsertedMoney; }
    public int getTotalSales() { return totalSales; }
    public Map<Integer, Integer> getCashVault() { return cashVault; }

    public String insertMoney(int amount) {
        if (!cashVault.containsKey(amount)) {
            return "エラー: 使用できない硬貨・紙幣です。";
        }
        if (this.currentInsertedMoney + amount > MAX_MONEY) {
            return "エラー: 投入可能金額の上限（" + MAX_MONEY + "円）を超えます。";
        }

        this.currentInsertedMoney += amount;
        cashVault.put(amount, cashVault.get(amount) + 1);
        return amount + "円を投入しました。";
    }

    public String purchaseDrink(int id) {
        Drink selected = null;
        for (Drink d : drinks) {
            if (d.getId() == id) { selected = d; break; }
        }

        if (selected == null) return "エラー: 存在しない商品番号です。";
        if (selected.isSoldOut()) return "エラー: 「" + selected.getName() + "」は売り切れです。";
        if (this.currentInsertedMoney < selected.getPrice()) {
            return "エラー: 投入金額が足りません。あと " + (selected.getPrice() - this.currentInsertedMoney) + "円 必要です。";
        }

        int changeNeeded = this.currentInsertedMoney - selected.getPrice();
        if (!canMakeChange(changeNeeded)) {
            return "エラー: 自販機内の釣銭（硬貨）が不足しているため、購入できません。";
        }

        selected.decreaseStock();
        this.currentInsertedMoney -= selected.getPrice();
        this.totalSales += selected.getPrice();
        
        writeLog(selected.getName(), selected.getPrice());

        return "「" + selected.getName() + "」を購入しました！";
    }

    public Map<Integer, Integer> refund() {
        int change = this.currentInsertedMoney;
        Map<Integer, Integer> refundDetails = new LinkedHashMap<>();
        
        for (int denomination : cashVault.keySet()) {
            if (change == 0) break;
            
            int countNeeded = change / denomination;
            int actualCount = Math.min(countNeeded, cashVault.get(denomination));
            
            if (actualCount > 0) {
                refundDetails.put(denomination, actualCount);
                change -= (denomination * actualCount);
                cashVault.put(denomination, cashVault.get(denomination) - actualCount);
            }
        }
        
        this.currentInsertedMoney = 0;
        return refundDetails;
    }

    private boolean canMakeChange(int change) {
        for (Map.Entry<Integer, Integer> entry : cashVault.entrySet()) {
            int denomination = entry.getKey();
            int availableCoins = entry.getValue();
            int needed = change / denomination;
            change -= denomination * Math.min(needed, availableCoins);
        }
        return change == 0;
    }

    public int collectSales() {
        int collected = this.totalSales;
        this.totalSales = 0;
        return collected;
    }

    private void writeLog(String drinkName, int price) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timestamp = now.format(formatter);
            
            pw.println(timestamp + "," + drinkName + "," + price + "円");
            
        } catch (IOException e) {
            System.out.println("【システム警告】CSVログの書き込みに失敗しました: " + e.getMessage());
        }
    }
}

// ==========================================
// 3. メイン画面・UI実行クラス（メニュー切り分け版）
// ==========================================
public class VendingMachineApp {
    private static final String ADMIN_PASSWORD = "admin123";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        VendingMachineController vm = new VendingMachineController();

        System.out.println("=== 高機能自動販売機シミュレータ ===");

        while (true) {
            System.out.println("\n===========================================");
            System.out.println(" 投入金額: " + vm.getCurrentInsertedMoney() + "円 (上限: 1990円)");
            System.out.println("【現在の総売上表示】: " + vm.getTotalSales() + "円");
            System.out.println("-------------------------------------------");
            System.out.println("【商品メニュー】");
            
            for (Drink d : vm.getDrinks()) {
                String stockStr = d.isSoldOut() ? "[売り切れ]" : "[残り:" + d.getStock() + "個]";
                String mark = (!d.isSoldOut() && vm.getCurrentInsertedMoney() >= d.getPrice()) ? "★購入可" : "       ";
                System.out.printf("  %d: %-12s (%3d円) %s %s\n", d.getId(), d.getName(), d.getPrice(), stockStr, mark);
            }
            System.out.println("===========================================");
            // ★メニューを変更：3をお釣り、5をシステム終了に切り分けました
            System.out.println("【操作】 1:お金を投入 | 2:商品購入 | 3:お釣り（清算） | 4:システム終了 (99:管理者モード)");
            System.out.print("操作番号を選択してください > ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("硬貨・紙幣（10, 50, 100, 500, 1000）を入力 > ");
                    try {
                        int amount = Integer.parseInt(scanner.nextLine());
                        System.out.println(vm.insertMoney(amount));
                    } catch (NumberFormatException e) {
                        System.out.println("エラー: 数値を入力してください。");
                    }
                    break;

                case "2":
                    System.out.print("購入する商品の番号（1〜5）を入力 > ");
                    try {
                        int id = Integer.parseInt(scanner.nextLine());
                        System.out.println(vm.purchaseDrink(id));
                    } catch (NumberFormatException e) {
                        System.out.println("エラー: 数値を入力してください。");
                    }
                    break;

                case "3": // ★お釣り（清算）処理。終了せずに続行します
                    int totalChange = vm.getCurrentInsertedMoney();
                    if (totalChange == 0) {
                        System.out.println("投入金額が0円のため、お釣りはありません。");
                    } else {
                        Map<Integer, Integer> details = vm.refund();
                        System.out.println("\nお釣り合計: " + totalChange + "円 を返却しました。");
                        if (!details.isEmpty()) {
                            System.out.println("【金種内訳】");
                            details.forEach((k, v) -> System.out.println("  " + k + "円: " + v + "枚"));
                        }
                    }
                    break;

                case "5": // ★システム終了処理。ここを選んだ時だけプログラムが終了します
                    // もしお金を入れたまま終了しようとした場合は自動でお釣りを出す親切設計
                    int remainingMoney = vm.getCurrentInsertedMoney();
                    if (remainingMoney > 0) {
                        Map<Integer, Integer> details = vm.refund();
                        System.out.println("\n未清算の投入金額があります。お釣り合計: " + remainingMoney + "円 を返却しました。");
                        if (!details.isEmpty()) {
                            details.forEach((k, v) -> System.out.println("  " + k + "円: " + v + "枚"));
                        }
                    }
                    System.out.println("シミュレータを終了します。ありがとうございました。");
                    scanner.close();
                    return;

                case "99":
                    System.out.print("管理者パスワードを入力してください > ");
                    String inputPass = scanner.nextLine();
                    if (!ADMIN_PASSWORD.equals(inputPass)) {
                        System.out.println("エラー: パスワードが違います。");
                        break;
                    }
                    runAdminMenu(scanner, vm);
                    break;

                default:
                    System.out.println("エラー: 正しいメニュー番号を入力してください。");
                    break;
            }
        }
    }

    private static void runAdminMenu(Scanner scanner, VendingMachineController vm) {
        while (true) {
            System.out.println("\n--- 管理者メニュー ---");
            System.out.println("1: 売上金の回収 | 2: 商品の補充 | 3: 釣銭金庫の残数確認 | 4: 管理者メニュー終了");
            System.out.print("操作番号を選択 > ");
            String subChoice = scanner.nextLine();

            if ("1".equals(subChoice)) {
                int collected = vm.collectSales();
                System.out.println("売上金 " + collected + "円 を回収しました。（現在の売上は0円にリセットされました）");
            } else if ("2".equals(subChoice)) {
                try {
                    System.out.print("補充する商品の番号（1〜5）を入力 > ");
                    int id = Integer.parseInt(scanner.nextLine());
                    System.out.print("補充する個数を入力 > ");
                    int count = Integer.parseInt(scanner.nextLine());
                    if (id >= 1 && id <= vm.getDrinks().size()) {
                        vm.getDrinks().get(id - 1).addStock(count);
                        System.out.println("「" + vm.getDrinks().get(id - 1).getName() + "」を補充しました。");
                    } else {
                        System.out.println("不正な商品番号です。");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("エラー: 数値を入力してください。");
                }
            } else if ("3".equals(subChoice)) {
                System.out.println("【釣銭金庫内 残数一覧】");
                vm.getCashVault().forEach((k, v) -> System.out.println("  " + k + "円の残数: " + v + "枚"));
            } else if ("4".equals(subChoice)) {
                System.out.println("管理者メニューを終了します。");
                break;
            } else {
                System.out.println("正しい番号を入力してください。");
            }
        }
    }
}