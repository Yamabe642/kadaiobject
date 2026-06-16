package リポジトリー;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// ==========================================
// 1. 商品クラス（Drink）
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

    // 在庫を1減らす
    public void decreaseStock() {
        if (this.stock > 0) {
            this.stock--;
        }
    }

    // 在庫があるか確認
    public boolean hasStock() {
        return this.stock > 0;
    }
}

// ==========================================
// 2. 自動販売機制御クラス（VendingMachineController）
// ==========================================
class VendingMachineController {
    private final List<Drink> showcase;
    private int insertedMoney;

    public VendingMachineController() {
        this.showcase = new ArrayList<>();
        this.insertedMoney = 0;
        initializeDrinks();
    }

    // 初期商品ラインナップの登録
    private void initializeDrinks() {
        showcase.add(new Drink(1, "コーラ", 150, 5));
        showcase.add(new Drink(2, "緑茶", 130, 3));
        showcase.add(new Drink(3, "ミネラルウォーター", 110, 0)); // テスト用に在庫0
        showcase.add(new Drink(4, "缶コーヒー", 120, 1));
    }

    public List<Drink> getShowcase() { return showcase; }
    public int getInsertedMoney() { return insertedMoney; }

    // お金を投入する（硬貨・紙幣のバリデーション）
    public boolean insertMoney(int amount) {
        // 自販機で一般的に使えるお金（10円、50円、100円、500円、1000円）
        if (amount == 10 || amount == 50 || amount == 100 || amount == 500 || amount == 1000) {
            this.insertedMoney += amount;
            return true;
        }
        return false; // 使用不可なお金
    }

    // 商品を購入する
    public String purchaseDrink(int drinkId) {
        Drink selected = null;
        for (Drink d : showcase) {
            if (d.getId() == drinkId) {
                selected = d;
                break;
            }
        }

        if (selected == null) {
            return "エラー: 存在しない商品番号です。";
        }
        if (!selected.hasStock()) {
            return "エラー: 「" + selected.getName() + "」は売り切れです。";
        }
        if (insertedMoney < selected.getPrice()) {
            return "エラー: 投入金額が足りません。あと " + (selected.getPrice() - insertedMoney) + "円 必要です。";
        }

        // 購入処理
        selected.decreaseStock();
        insertedMoney -= selected.getPrice();
        return "「" + selected.getName() + "」を購入しました！";
    }

    // お釣りを出して精算する
    public int refund() {
        int change = this.insertedMoney;
        this.insertedMoney = 0;
        return change;
    }
}

// ==========================================
// 3. メイン画面・UI実行クラス（VendingMachineApp）
// ==========================================
public class VendingMachineApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        VendingMachineController vm = new VendingMachineController();

        System.out.println("=== 自動販売機シミュレータ ===");

        while (true) {
            System.out.println("\n-------------------------------------------");
            System.out.println("現在の投入金額: " + vm.getInsertedMoney() + "円");
            System.out.println("【商品ラインナップ】");
            for (Drink d : vm.getShowcase()) {
                String stockStatus = d.hasStock() ? "[在庫:" + d.getStock() + "]" : "[売り切れ]";
                // 購入可能なものには「★」を表示
                String buyable = (d.hasStock() && vm.getInsertedMoney() >= d.getPrice()) ? "★可" : "  ";
                System.out.printf("  %d: %-10s (%3d円) %s %s\n", d.getId(), d.getName(), d.getPrice(), stockStatus, buyable);
            }
            System.out.println("-------------------------------------------");
            System.out.println("【メニュー】 1:お金を入れる | 2:商品を選ぶ | 3:お釣り（終了）");
            System.out.print("操作番号を入力してください > ");
            
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("投入する金額（10, 50, 100, 500, 1000）を入力してください > ");
                    try {
                        int amount = Integer.parseInt(scanner.nextLine());
                        if (vm.insertMoney(amount)) {
                            System.out.println(amount + "円を投入しました。");
                        } else {
                            System.out.println("エラー: その硬貨・紙幣は使用できません（10円〜1000円札のみ）。");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("エラー: 数値を入力してください。");
                    }
                    break;

                case "2":
                    System.out.print("購入する商品の番号を入力してください > ");
                    try {
                        int drinkId = Integer.parseInt(scanner.nextLine());
                        String result = vm.purchaseDrink(drinkId);
                        System.out.println(result);
                    } catch (NumberFormatException e) {
                        System.out.println("エラー: 数値を入力してください。");
                    }
                    break;

                case "3":
                    int change = vm.refund();
                    System.out.println("お釣り: " + change + "円 をお受け取りください。");
                    System.out.println("シミュレータを終了します。ご利用ありがとうございました。");
                    scanner.close();
                    return;

                default:
                    System.out.println("エラー: 1〜3の番号を選択してください。");
                    break;
            }
        }
    }
}