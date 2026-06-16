package リポジトリー;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// 選手情報を管理するクラス（問題2の要件）
class Player {
    private final String name;
    private final List<Integer> strokes;

    public Player(String name) {
        this.name = name;
        this.strokes = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void addStroke(int stroke) {
        this.strokes.add(stroke);
    }

    // 各ホールのパー値と照らし合わせて総スコアを計算
    public int calculateTotalScore(int[] pars) {
        int total = 0;
        for (int i = 0; i < strokes.size(); i++) {
            total += (strokes.get(i) - pars[i]);
        }
        return total;
    }
}

public class GolfGame {
    // 1〜18ホールの固定パー値
    private static final int[] PARS = {4, 4, 3, 4, 5, 4, 5, 3, 4, 4, 3, 4, 5, 4, 3, 4, 5, 4};

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Input > ");
            String input = scanner.nextLine();

            // バリデーションチェック
            if (input == null || input.trim().isEmpty()) {
                System.out.println("エラー: 空入力です。再度入力してください。");
                continue;
            }

            // スペースの読み飛ばしとカンマ分割
            String[] tokens = input.replace(" ", "").split(",");
            
            // 項目数チェック（選手名2つ + スコア18ホール×2人 = 38項目が必要）
            if (tokens.length < 38) {
                System.out.println("エラー: 入力項目が足りません。選手名2つと36個の数値を入力してください。");
                continue;
            }

            // 選手名の取得
            String p1Name = tokens[0];
            String p2Name = tokens[1];

            // スコア部分の数値チェック
            boolean hasError = false;
            List<Integer> allStrokes = new ArrayList<>();
            for (int i = 2; i < 38; i++) {
                try {
                    int stroke = Integer.parseInt(tokens[i]);
                    if (stroke <= 0) {
                        System.out.println("エラー: 0以下の整数が含まれています。再度入力してください。");
                        hasError = true;
                        break;
                    }
                    allStrokes.add(stroke);
                } catch (NumberFormatException e) {
                    System.out.println("エラー: 不正な文字、または数値以外が含まれています。再度入力してください。");
                    hasError = true;
                    break;
                }
            }

            if (hasError) continue;

            // 選手オブジェクトの作成とスコア割り当て
            Player player1 = new Player(p1Name);
            Player player2 = new Player(p2Name);

            // 3個目〜20個目を1番目の選手、21個目〜38個目を2番目の選手に割り当て
            for (int i = 0; i < 18; i++) {
                player1.addStroke(allStrokes.get(i));
                player2.addStroke(allStrokes.get(i + 18));
            }

            // スコア計算
            int p1Score = player1.calculateTotalScore(PARS);
            int p2Score = player2.calculateTotalScore(PARS);

            // 結果出力
            System.out.println(player1.getName() + "のスコア: " + formatScore(p1Score));
            System.out.println(player2.getName() + "のスコア: " + formatScore(p2Score));

            // 勝敗判定（ゴルフはスコアが低い方が勝ち）
            if (p1Score < p2Score) {
                System.out.println("勝者: " + player1.getName());
            } else if (p2Score < p1Score) {
                System.out.println("勝者: " + player2.getName());
            } else {
                System.out.println("結果: 引き分け");
            }
            break; // 正常終了
        }
        scanner.close();
    }

    // スコアの表示表記を整形（例: +9, -1, +-0）
    private static String formatScore(int score) {
        if (score > 0) return "+" + score;
        if (score < 0) return String.valueOf(score);
        return "+-0";
    }
}