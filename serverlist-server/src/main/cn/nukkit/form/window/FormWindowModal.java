package cn.nukkit.form.window;

import org.apache.commons.lang3.StringUtils;

import cn.nukkit.form.response.FormResponseModal;

public class FormWindowModal extends FormWindow {

    private final String type = "modal"; //This variable is used for JSON import operations. Do NOT delete :) -- @Snake1999
    private String title = "";
    private String content = "";
    private String button1 = "";
    private String button2 = "";

    private FormResponseModal response = null;

    public FormWindowModal(String title, String content, String trueButtonText, String falseButtonText) {
        this.title = title;
        this.content = content;
        this.button1 = trueButtonText;
        this.button2 = falseButtonText;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getButton1() {
        return button1;
    }

    public void setButton1(String button1) {
        this.button1 = button1;
    }

    public String getButton2() {
        return button2;
    }

    public void setButton2(String button2) {
        this.button2 = button2;
    }

    public FormResponseModal getResponse() {
        return response;
    }

    public void setResponse(String data) {
    	String safeData = StringUtils.trim(data);
        if (StringUtils.equals(safeData, "null")) {
            closed = true;
            return;
        }
        if (StringUtils.equals(safeData, "true")) response = new FormResponseModal(0, button1);
        else response = new FormResponseModal(1, button2);
    }

}
