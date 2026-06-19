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
import java.util.function.Consumer;

// ==========================================
// 1. 商品情報を管理するクラス (旧: Drink)
// ==========================================
class Product {
    private final int id, price;
    private final String name;
    private int stock;

    public Product(int id, String name, int price, int stock) {
        this.id = id; this.name = name; this.price = price; this.stock = stock;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getStock() { return stock; }
    public boolean isSoldOut() { return stock <= 0; }
    
    public void decreaseStock() { if (stock > 0) stock--; }
    public void addStock(int amount) { if (amount > 0) stock += amount; }
}

// ==========================================
// 2. 自動販売機のロジック・金庫・ログ管理クラス (旧: VendingMachineController)
// ==========================================
class MachineCore {
    private final List<Product> products = new ArrayList<>();
    private final Map<Integer, Integer> cashVault = new LinkedHashMap<>();
    private int currentInsertedMoney = 0;
    private int totalSales = 0;
    
    private static final int MAX_MONEY = 1990;
    private static final String LOG_FILE = "vending_machine_log.csv";

    public MachineCore() {
        products.add(new Product(1, "缶コーヒー", 110, 5));
        products.add(new Product(2, "缶お茶", 120, 5));
        products.add(new Product(3, "スポーツドリンク", 130, 3));
        products.add(new Product(4, "炭酸ソーダ", 150, 2));
        products.add(new Product(5, "果汁100%ジュース", 160, 4));

        for (int coin : List.of(1000, 500, 100, 50, 10)) cashVault.put(coin, 10);
    }

    public List<Product> getProducts() { return products; }
    public int getCurrentInsertedMoney() { return currentInsertedMoney; }
    public int getTotalSales() { return totalSales; }
    public Map<Integer, Integer> getCashVault() { return cashVault; }

    public String insertMoney(int amount) {
        if (!cashVault.containsKey(amount)) return "エラー: 使用できない硬貨・紙幣です。";
        if (currentInsertedMoney + amount > MAX_MONEY) return "エラー: 投入可能金額の上限（" + MAX_MONEY + "円）を超えます。";

        currentInsertedMoney += amount;
        cashVault.put(amount, cashVault.get(amount) + 1);
        return amount + "円を投入しました。";
    }

    public String purchaseProduct(int id) {
        Product selected = products.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
        if (selected == null) return "エラー: 存在しない商品番号です。";
        if (selected.isSoldOut()) return "エラー: 「" + selected.getName() + "」は売り切れです。";
        if (currentInsertedMoney < selected.getPrice()) {
            return "エラー: 投入金額が足りません。あと " + (selected.getPrice() - currentInsertedMoney) + "円 必要です。";
        }

        int changeNeeded = currentInsertedMoney - selected.getPrice();
        if (!canMakeChange(changeNeeded)) return "エラー: 自販機内の釣銭（硬貨）が不足しているため、購入できません。";

        selected.decreaseStock();
        currentInsertedMoney -= selected.getPrice();
        totalSales += selected.getPrice();
        
        writeLog(selected.getName(), selected.getPrice());
        return "「" + selected.getName() + "」を購入しました！";
    }

    public Map<Integer, Integer> refund() {
        int change = currentInsertedMoney;
        Map<Integer, Integer> refundDetails = new LinkedHashMap<>();
        
        for (int denomination : cashVault.keySet()) {
            int actualCount = Math.min(change / denomination, cashVault.get(denomination));
            if (actualCount > 0) {
                refundDetails.put(denomination, actualCount);
                change -= (denomination * actualCount);
                cashVault.put(denomination, cashVault.get(denomination) - actualCount);
            }
        }
        currentInsertedMoney = 0;
        return refundDetails;
    }

    private boolean canMakeChange(int change) {
        for (var entry : cashVault.entrySet()) {
            change -= entry.getKey() * Math.min(change / entry.getKey(), entry.getValue());
        }
        return change == 0;
    }

    public int collectSales() {
        int collected = totalSales;
        totalSales = 0;
        return collected;
    }

    private void writeLog(String productName, int price) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            pw.println(timestamp + "," + productName + "," + price + "円");
        } catch (IOException e) {
            System.out.println("【システム警告】CSVログの書き込みに失敗しました: " + e.getMessage());
        }
    }
}

// ==========================================
// 3. メイン画面・UI実行クラス (旧: Test / VendingMachineApp)
// ==========================================
public class test {
    private static final String ADMIN_PASSWORD = "admin123";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        MachineCore core = new MachineCore();

        System.out.println("=== 自動販売機シミュレータ ===");

        while (true) {
            System.out.println("\n===========================================");
            System.out.println(" 投入金額: " + core.getCurrentInsertedMoney() + "円 (上限: 1990円)");
            System.out.println("【現在の総売上表示】: " + core.getTotalSales() + "円");
            System.out.println("-------------------------------------------");
            System.out.println("【商品メニュー】");
            
            for (Product p : core.getProducts()) {
                String stockStr = p.isSoldOut() ? "[売り切れ]" : "[残り:" + p.getStock() + "個]";
                String mark = (!p.isSoldOut() && core.getCurrentInsertedMoney() >= p.getPrice()) ? "★購入可" : "       ";
                System.out.printf("  %d: %-12s (%3d円) %s %s\n", p.getId(), p.getName(), p.getPrice(), stockStr, mark);
            }
            System.out.println("===========================================");
            System.out.println("【操作】 1:お金を投入 | 2:商品購入 | 3:お釣り（清算） | 4:システム終了 (99:管理者モード)");
            System.out.print("操作番号を選択してください > ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> executeWithNumberInput(scanner, "硬貨・紙幣（10, 50, 100, 500, 1000）を入力 > ", amount -> System.out.println(core.insertMoney(amount)));
                case "2" -> executeWithNumberInput(scanner, "購入する商品の番号（1〜5）を入力 > ", id -> System.out.println(core.purchaseProduct(id)));
                case "3", "4" -> {
                    int change = core.getCurrentInsertedMoney();
                    if (change > 0) {
                        String prefix = "4".equals(choice) ? "未清算の投入金額があります。 " : "";
                        System.out.println("\n" + prefix + "お釣り合計: " + change + "円 を返却しました。");
                        core.refund().forEach((k, v) -> System.out.println("  " + k + "円: " + v + "枚"));
                    } else if ("3".equals(choice)) {
                        System.out.println("投入金額が0円のため、お釣りはありません。");
                    }
                    if ("4".equals(choice)) {
                        System.out.println("シミュレータを終了します。ありがとうございました。");
                        scanner.close();
                        return;
                    }
                }
                case "99" -> {
                    System.out.print("管理者パスワードを入力してください > ");
                    if (ADMIN_PASSWORD.equals(scanner.nextLine())) {
                        runAdminMenu(scanner, core);
                    } else {
                        System.out.println("エラー: パスワードが違います。");
                    }
                }
                default -> System.out.println("エラー: 正しいメニュー番号を入力してください。");
            }
        }
    }

    // 数値入力とエラーハンドリングの共通化メソッド
    private static void executeWithNumberInput(Scanner scanner, String message, Consumer<Integer> action) {
        System.out.print(message);
        try {
            action.accept(Integer.parseInt(scanner.nextLine()));
        } catch (NumberFormatException e) {
            System.out.println("エラー: 数値を入力してください。");
        }
    }

    private static void runAdminMenu(Scanner scanner, MachineCore core) {
        while (true) {
            System.out.println("\n--- 管理者メニュー ---");
            System.out.println("1: 売上金の回収 | 2: 商品の補充 | 3: 釣銭金庫の残数確認 | 4: 管理者メニュー終了");
            System.out.print("操作番号を選択 > ");
            
            switch (scanner.nextLine()) {
                case "1" -> System.out.println("売上金 " + core.collectSales() + "円 を回収しました。（現在の売上は0円にリセットされました）");
                case "2" -> executeWithNumberInput(scanner, "補充する商品の番号（1〜5）を入力 > ", id -> 
                            executeWithNumberInput(scanner, "補充する個数を入力 > ", count -> {
                                if (id >= 1 && id <= core.getProducts().size()) {
                                    core.getProducts().get(id - 1).addStock(count);
                                    System.out.println("「" + core.getProducts().get(id - 1).getName() + "」を補充しました。");
                                } else {
                                    System.out.println("不正な商品番号です。");
                                }
                            }));
                case "3" -> {
                    System.out.println("【釣銭金庫内 残数一覧】");
                    core.getCashVault().forEach((k, v) -> System.out.println("  " + k + "円の残数: " + v + "枚"));
                }
                case "4" -> {
                    System.out.println("管理者メニューを終了します。");
                    return;
                }
                default -> System.out.println("正しい番号を入力してください。");
            }
        }
    }
}