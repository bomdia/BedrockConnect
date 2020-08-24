package cn.nukkit.form.window;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import cn.nukkit.form.response.FormResponse;

public abstract class FormWindow {

    private static final Gson GSON = new Gson();
    
    protected boolean closed = false;

	protected static final Logger log = LogManager.getLogger(FormWindow.class.getName());
    
    public String getJSONData() {
        return FormWindow.GSON.toJson(this);
    }

    public abstract void setResponse(String data);

    public abstract FormResponse getResponse();

    public boolean wasClosed() {
        return closed;
    }

}
