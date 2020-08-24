package cn.nukkit.form.window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import cn.nukkit.form.element.Element;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.element.ElementSlider;
import cn.nukkit.form.element.ElementStepSlider;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.response.FormResponseData;

public class FormWindowCustom extends FormWindow {

    private final String type = "custom_form"; //This variable is used for JSON import operations. Do NOT delete :) -- @Snake1999
    private String title = "";
    private ElementButtonImageData icon;
    private List<Element> content;

    private FormResponseCustom response;

    public FormWindowCustom(String title) {
        this(title, new ArrayList<>());
    }

    public FormWindowCustom(String title, List<Element> contents) {
        this(title, contents, (ElementButtonImageData) null);
    }

    public FormWindowCustom(String title, List<Element> contents, String icon) {
        this(title, contents, icon.isEmpty() ? null : new ElementButtonImageData(ElementButtonImageData.IMAGE_DATA_TYPE_URL, icon));
    }

    public FormWindowCustom(String title, List<Element> contents, ElementButtonImageData icon) {
        this.title = title;
        this.content = contents;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Element> getElements() {
        return content;
    }

    public void addElement(Element element) {
        content.add(element);
    }

    public ElementButtonImageData getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        if (!icon.isEmpty()) this.icon = new ElementButtonImageData(ElementButtonImageData.IMAGE_DATA_TYPE_URL, icon);
    }

    public void setIcon(ElementButtonImageData icon) {
        this.icon = icon;
    }

    public FormResponseCustom getResponse() {
        return response;
    }

    public void setResponse(String data) {
    	String safeData = StringUtils.trim(data);
        if (StringUtils.equals(safeData, "null")) {
            this.closed = true;
            return;
        }

        List<String> elementResponses = new Gson().fromJson(data, new TypeToken<List<String>>() {
        }.getType());
        //elementResponses.remove(elementResponses.size() - 1); //submit button //maybe mojang removed that?

        int i = 0;

        int cLabel = 0;
        int cDropdown = 0;
        int cInput = 0;
        int cSlider = 0;
        int cStepSlider = 0;
        int cToggle = 0;
        
        HashMap<Integer, FormResponseData> dropdownResponses = new HashMap<>();
        HashMap<Integer, String> inputResponses = new HashMap<>();
        HashMap<Integer, Float> sliderResponses = new HashMap<>();
        HashMap<Integer, FormResponseData> stepSliderResponses = new HashMap<>();
        HashMap<Integer, Boolean> toggleResponses = new HashMap<>();
        HashMap<Integer, Object> responses = new HashMap<>();
        HashMap<Integer, String> labelResponses = new HashMap<>();

        for (String elementData : elementResponses) {
            if (i >= content.size()) {
                break;
            }

            Element e = content.get(i);
            if (e == null) break;
            if (e instanceof ElementLabel) {
                labelResponses.put(cLabel, ((ElementLabel) e).getText());
                responses.put(i, ((ElementLabel) e).getText());
                cLabel ++;
            } else if (e instanceof ElementDropdown) {
                String answer = ((ElementDropdown) e).getOptions().get(Integer.parseInt(elementData));
                dropdownResponses.put(cDropdown, new FormResponseData(Integer.parseInt(elementData), answer));
                responses.put(i, answer);
                cDropdown ++;
            } else if (e instanceof ElementInput) {
                inputResponses.put(cInput, elementData);
                responses.put(i, elementData);
                cInput ++;
            } else if (e instanceof ElementSlider) {
                Float answer = Float.parseFloat(elementData);
                sliderResponses.put(cSlider, answer);
                responses.put(i, answer);
                cSlider ++;
            } else if (e instanceof ElementStepSlider) {
                String answer = ((ElementStepSlider) e).getSteps().get(Integer.parseInt(elementData));
                stepSliderResponses.put(cStepSlider, new FormResponseData(Integer.parseInt(elementData), answer));
                responses.put(i, answer);
                cStepSlider ++;
            } else if (e instanceof ElementToggle) {
                Boolean answer = Boolean.parseBoolean(elementData);
                toggleResponses.put(cToggle, answer);
                responses.put(i, answer);
                cToggle ++;
            }
            i++;
        }

        this.response = new FormResponseCustom(responses, dropdownResponses, inputResponses,
                sliderResponses, stepSliderResponses, toggleResponses, labelResponses);
    }

    /**
     * Set Elements from Response
     * Used on ServerSettings Form Response. After players set settings, we need to sync these settings to the server.
     */
    public void setElementsFromResponse() {
        if (this.response != null) {
            this.response.getResponses().forEach((i, response) -> {
                Element e = content.get(i);
                if (e != null) {
                    if (e instanceof ElementDropdown) {
                        ((ElementDropdown) e).setDefaultOptionIndex(((ElementDropdown) e).getOptions().indexOf(response));
                    } else if (e instanceof ElementInput) {
                        ((ElementInput) e).setDefaultText((String)response);
                    } else if (e instanceof ElementSlider) {
                        ((ElementSlider) e).setDefaultValue((Float)response);
                    } else if (e instanceof ElementStepSlider) {
                        ((ElementStepSlider) e).setDefaultOptionIndex(((ElementStepSlider) e).getSteps().indexOf(response));
                    } else if (e instanceof ElementToggle) {
                        ((ElementToggle) e).setDefaultValue((Boolean)response);
                    }
                }
            });
        }
    }

}
