package リポジトリー;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

// ==========================================
// 1. 銀行口座クラス（BankAccount）
// ==========================================
// 口座番号、暗証番号、残高を管理する（カプセル化）
class BankAccount {
    private final String accountNumber;
    private final String pin; // 暗証番号
    private int balance;      // 残高

    public BankAccount(String accountNumber, String pin, int initialBalance) {
        this.accountNumber = accountNumber;
        this.pin = pin;
        this.balance = initialBalance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    // 暗証番号が一致しているかチェック
    public boolean authenticate(String inputPin) {
        return this.pin.equals(inputPin);
    }

    public int getBalance() {
        return balance;
    }

    // 預け入れ
    public void deposit(int amount) {
        if (amount > 0) {
            balance += amount;
        }
    }

    // 引き出し（残高が足りる場合のみ減算してtrueを返す）
    public boolean withdraw(int amount) {
        if (amount <= balance) {
            balance -= amount;
            return true;
        }
        return false;
    }
}

// ==========================================
// 2. ATM制御クラス（AtmController）
// ==========================================
// ATMの「状態」や「認証・取引のシナリオ（シーケンス）」を制御する
class AtmController {
    private final Map<String, BankAccount> bankDatabase; // 模擬銀行データベース
    private BankAccount currentAccount;                  // 現在操作中の口座
    private boolean isAuthenticated;                     // 認証フラグ

    public AtmController() {
        bankDatabase = new HashMap<>();
        // テスト用データを登録（口座番号: 1234, 暗証番号: 1111, 残高: 50000円）
        bankDatabase.put("1234", new BankAccount("1234", "1111", 50000));
        // もう一つテスト用データ（口座番号: 5678, 暗証番号: 2222, 残高: 100000円）
        bankDatabase.put("5678", new BankAccount("5678", "2222", 100000));
        
        resetSession();
    }

    // セッションの初期化
    public void resetSession() {
        this.currentAccount = null;
        this.isAuthenticated = false;
    }

    // カード挿入（口座番号の入力）のシミュレート
    public boolean insertCard(String accountNumber) {
        if (bankDatabase.containsKey(accountNumber)) {
            currentAccount = bankDatabase.get(accountNumber);
            return true;
        }
        return false;
    }

    // 暗証番号の認証
    public boolean enterPin(String pin) {
        if (currentAccount != null && currentAccount.authenticate(pin)) {
            isAuthenticated = true;
            return true;
        }
        return false;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    // 残高照会
    public int checkBalance() {
        return currentAccount.getBalance();
    }

    // 預け入れの実行
    public void executeDeposit(int amount) {
        if (currentAccount != null && amount > 0) {
            currentAccount.deposit(amount);
        }
    }

    // 引き出しの実行
    public boolean executeWithdraw(int amount) {
        if (currentAccount != null && amount > 0) {
            return currentAccount.withdraw(amount);
        }
        return false;
    }
}

// ==========================================
// 3. メイン画面・UI実行クラス（AtmSystem）
// ==========================================
public class AtmSystem {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        AtmController atm = new AtmController();

        System.out.println("--- ATMシステムへようこそ ---");

        // 1. 口座番号の入力（カード挿入の代わり）
        while (true) {
            System.out.print("口座番号を入力してください（テスト用: 1234）> ");
            String accountNumber = scanner.nextLine();
            if (atm.insertCard(accountNumber)) {
                break;
            }
            System.out.println("エラー: 存在しない口座番号です。再度入力してください。\n");
        }

        // 2. 暗証番号の入力（認証処理）
        while (true) {
            System.out.print("4桁の暗証番号を入力してください（テスト用: 1111）> ");
            String pin = scanner.nextLine();
            if (atm.enterPin(pin)) {
                System.out.println("認証に成功しました。");
                break;
            }
            System.out.println("エラー: 暗証番号が一致しません。再度入力してください。\n");
        }

        // 3. 取引メニューのループ処理
        while (atm.isAuthenticated()) {
            System.out.println("\n【メニュー】 1:残高照会 | 2:預け入れ | 3:引き出し | 4:終了");
            System.out.print("操作番号を選択してください > ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1": // 残高照会
                    System.out.println("現在の残高は " + atm.checkBalance() + "円 です。");
                    break;

                case "2": // 預け入れ
                    System.out.print("預け入れる金額を入力してください > ");
                    try {
                        int depAmount = Integer.parseInt(scanner.nextLine());
                        if (depAmount <= 0) {
                            System.out.println("エラー: 1円以上の正しい金額を入力してください。");
                        } else {
                            atm.executeDeposit(depAmount);
                            System.out.println(depAmount + "円の預け入れが完了しました。");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("エラー: 数値を入力してください。");
                    }
                    break;

                case "3": // 引き出し
                    System.out.print("引き出す金額を入力してください > ");
                    try {
                        int withAmount = Integer.parseInt(scanner.nextLine());
                        if (withAmount <= 0) {
                            System.out.println("エラー: 1円以上の正しい金額を入力してください。");
                        } else {
                            if (atm.executeWithdraw(withAmount)) {
                                System.out.println(withAmount + "円を引き出しました。お札をお受け取りください。");
                            } else {
                                System.out.println("エラー: 残高不足です。現在の残高: " + atm.checkBalance() + "円");
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("エラー: 数値を入力してください。");
                    }
                    break;

                case "4": // 終了
                    System.out.println("カードと明細をお受け取りください。ご利用ありがとうございました。");
                    atm.resetSession();
                    scanner.close();
                    return;

                default:
                    System.out.println("エラー: 1〜4の番号を選択してください。");
                    break;
            }
        }
    }
}