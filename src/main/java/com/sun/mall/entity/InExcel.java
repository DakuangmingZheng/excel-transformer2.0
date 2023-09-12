package com.sun.mall.entity;

import com.sun.mall.utils.ExcelImport;
import lombok.Data;

@Data
public class InExcel {
    @ExcelImport("テストプラットフォーム")
    private String recordingType;

    @ExcelImport("録画ファイルのサイズ")
    private String recordingSize;

    @ExcelImport("最大値")
    private String maxSize;

    @ExcelImport("閾値")
    private String threshold;

    @ExcelImport("録画アクション")
    private String action;

    @ExcelImport("録画ウィンドウサイズ")
    private String screenSize;

    @ExcelImport("テストできるか")
    private String aim;

    @ExcelImport("NO")
    private String no;

    public String getStep() {
        return
                """
                1.「録画を開始」ボタンをクリックしてください。
                """;
    }

    public String getConditions() {

        return
        """
        1.テストプラットフォーム：%s
        2.録画ファイルのサイズ：%s
        3.最大値:%s
        4.閾値:%s
        5.録画アクション:%s
        """.formatted(recordingType,recordingSize,maxSize,threshold,action);

    }

    public String getAim() {
        if ( "500MB以下または500MBと等しい".equals(maxSize)||"2MB以下または2MBと等しい".equals(threshold)||"50MBを超える".equals(threshold)){
            return
                    """
                    エラーメッセージを表示します。「録画機能のパラメータ設定が正しくありません。管理者にパラメータを修正してもらってください。今回の録画は一時的に適切なパラメータ値を使用して行われます。」
                    """;
        }else{
            return
                    """
                    録画の開始に成功しました。
                    """;
        }
    }
}
