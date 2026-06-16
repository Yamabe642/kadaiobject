package リポジトリー;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// 各ホールの情報を管理するクラス
class Hole {
    private final int holeNumber;
    private final int par;
    private int strokes;

    public Hole(int holeNumber, int par) {
        this.holeNumber = holeNumber;
        this.par = par;
    }

    public void setStrokes(int strokes) {
        this.strokes = strokes;
    }

    // 各ホールのスコア（打数 - パー）を計算
    public int getScore() {
        return this.strokes - this.par;
    }
}

// ゴルフスコアの計算とバリデーションを管理するクラス
class GolfScoreCalculator {
    // 1〜18ホールの固定パー値
    private static final int[] PARS = {4, 4, 3, 4, 5, 4, 5, 3, 4, 4, 3, 4, 5, 4, 3, 4, 5, 4};
    private final List<Hole> holes;

    public GolfScoreCalculator() {
        this.holes = new ArrayList<>();
        for (int i = 0; i < PARS.length; i++) {
            holes.add(new Hole(i + 1, PARS[i]));
        }
    }

    // 入力文字列をパース・チェックし、スコアを計算して表示する
    public boolean processInput(String input) {
        // 空入力チェック
        if (input == null || input.trim().isEmpty()) {
            System.out.println("エラー: 入力が空です。再度入力してください。");
            return false;
        }

        // 許可されていない文字が含まれていないかチェック
        // (数字、カンマ、半角スペース、改行等以外はエラー)
        if (!input.matches("[0-9, ]+")) {
            System.out.println("エラー: 入力可能な文字は「数字」「カンマ」「半角スペース」のみです。");
            return false;
        }

        // 半角スペースを読み飛ばし、カンマで分割
        String[] tokens = input.replace(" ", "").split(",");
        List<Integer> validStrokes = new ArrayList<>();

        for (String token : tokens) {
            if (token.isEmpty()) continue; // カンマが連続した場合などはスキップ
            
            try {
                int stroke = Integer.parseInt(token);
                // 0以下の整数チェック
                if (stroke <= 0) {
                    System.out.println("エラー: 0以下の整数が入力されています。再度入力してください。");
                    return false;
                }
                validStrokes.add(stroke);
            } catch (NumberFormatException e) {
                System.out.println("エラー: 数値のパースに失敗しました。");
                return false;
            }
        }

        if (validStrokes.isEmpty()) {
            System.out.println("エラー: 有効な数値が入力されていません。");
            return false;
        }

        // 19番目以降の数字は無視し、最大18ホール分を計算
        int totalHoles = Math.min(validStrokes.size(), PARS.length);
        int totalScore = 0;

        for (int i = 0; i < totalHoles; i++) {
            Hole hole = holes.get(i);
            hole.setStrokes(validStrokes.get(i));
            totalScore += hole.getScore();
        }

        // 結果出力
        System.out.println("終了したホール数: " + totalHoles);
        System.out.println("スコア: " + totalScore);
        return true;
    }
}

// プログラムを実行するメインクラス
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            GolfScoreCalculator calculator = new GolfScoreCalculator();
            System.out.print("Input > ");
            String input = scanner.nextLine();
            
            // 処理が正常に成功した場合はループを抜けて終了
            if (calculator.processInput(input)) {
                break;
            }
            System.out.println(); // エラー時の改行
        }
        
        scanner.close();
    }
}