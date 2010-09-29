/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * There are special exceptions to the terms and conditions of the GPL
 * as it is applied to this software. View the full text of the
 * exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
 * software distribution.
 *
 * This program is distributed WITHOUT ANY WARRANTY; and without the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
 * or write to:
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307  USA
 *
 */

package com.manydesigns.elements.fields;

import com.manydesigns.elements.annotations.Select;
import com.manydesigns.elements.json.JsonBuffer;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.Util;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Map;

/*
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 */
public class SelectField extends AbstractField {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    protected OptionProvider optionProvider;
    protected SelectField previousSelectField;
    protected SelectField nextSelectField;
    protected int optionProviderIndex;

    protected String comboLabel;


    //**************************************************************************
    // Costruttori
    //**************************************************************************
    public SelectField(PropertyAccessor accessor) {
        this(accessor, null);
    }

    public SelectField(PropertyAccessor accessor, String prefix) {
        super(accessor, prefix);

        Select annotation = accessor.getAnnotation(Select.class);
        if (annotation != null) {
            optionProvider = DefaultOptionProvider.create(
                    accessor.getName(), 1,
                    annotation.values(), annotation.labels());
        }

        comboLabel = MessageFormat.format("-- Select {0} --", label);
    }

    //**************************************************************************
    // Implementazione di Component
    //**************************************************************************
    public void readFromRequest(HttpServletRequest req) {
        super.readFromRequest(req);

        if (mode.isView(immutable, autogenerated)) {
            return;
        }

        String stringValue = req.getParameter(inputName);
        Object value = Util.convertValue(stringValue, accessor.getType());
        optionProvider.setValue(optionProviderIndex, value);
    }

    public boolean validate() {
        if (mode.isView(immutable, autogenerated)
                || (mode.isBulk() && !bulkChecked)) {
            return true;
        }

        Object value = optionProvider.getValue(optionProviderIndex);
        if (required && value == null) {
            errors.add(getText("elements.error.field.required"));
            return false;
        }
        return true;
    }

    public void readFromObject(Object obj) {
        super.readFromObject(obj);
        try {
            Object value;
            if (obj == null) {
                value = null;
            } else {
                value = accessor.get(obj);
            }
            optionProvider.setValue(optionProviderIndex, value);
        } catch (IllegalAccessException e) {
            throw new Error(e);
        } catch (InvocationTargetException e) {
            throw new Error(e);
        }
    }

    public void writeToObject(Object obj) {
        Object value = optionProvider.getValue(optionProviderIndex);
        writeToObject(obj, value);
    }

    public void valueToXhtml(XhtmlBuffer xb) {
        if (mode.isView(immutable, autogenerated)) {
            valueToXhtmlView(xb);
        } else if (mode.isEdit()) {
            valueToXhtmlEdit(xb);
        } else if (mode.isPreview()) {
            valueToXhtmlPreview(xb);
        } else if (mode.isHidden()) {
            valueToXhtmlHidden(xb);
        } else {
            throw new IllegalStateException("Unknown mode: " + mode);
        }
    }

    private void valueToXhtmlEdit(XhtmlBuffer xb) {
        xb.openElement("select");
        xb.addAttribute("id", id);
        xb.addAttribute("name", inputName);
        if (nextSelectField != null) {
            String js = composeJs();
            xb.addAttribute("onChange", js);
        }

        Map<Object,String> options =
                optionProvider.getOptions(optionProviderIndex);
        Object objectValue = optionProvider.getValue(optionProviderIndex);

        boolean checked = (objectValue == null);
        if (!options.isEmpty()) {
            xb.writeOption("", checked, comboLabel);
        }

        for (Map.Entry<Object,String> option :
                options.entrySet()) {
            Object optionValue = option.getKey();
            String optionLabel = option.getValue();
            String optionValueString =
                    (String) Util.convertValue(optionValue, String.class);
            checked =  optionValue.equals(objectValue);
            xb.writeOption(optionValueString, checked, optionLabel);
        }
        xb.closeElement("select");
    }

    protected String composeJs() {
        SelectField rootField = this;
        while (rootField.previousSelectField != null) {
            rootField = rootField.previousSelectField;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(MessageFormat.format(
                "updateSelectOptions(''{0}'', {1}",
                StringEscapeUtils.escapeJavaScript(optionProvider.getName()),
                optionProviderIndex + 1));
        SelectField currentField = rootField;
        while (currentField != null) {
            sb.append(", '");
            sb.append(StringEscapeUtils.escapeJavaScript(currentField.getId()));
            sb.append("'");
            currentField = currentField.nextSelectField;
        }
        sb.append(");");
        return sb.toString();
    }

    public void valueToXhtmlPreview(XhtmlBuffer xb) {
        valueToXhtmlView(xb);
        valueToXhtmlHidden(xb);
    }

    private void valueToXhtmlHidden(XhtmlBuffer xb) {
        Object optionValue = optionProvider.getValue(optionProviderIndex);
        String optionStringValue =
                (String) Util.convertValue(optionValue, String.class);
        xb.writeInputHidden(inputName, optionStringValue);
    }

    public void valueToXhtmlView(XhtmlBuffer xb) {
        xb.openElement("div");
        xb.addAttribute("class", "value");
        xb.addAttribute("id", id);
        Map<Object,String> options =
                optionProvider.getOptions(optionProviderIndex);
        Object optionValue = optionProvider.getValue(optionProviderIndex);
        String optionLabel = options.get(optionValue);
        xb.write(optionLabel);
        xb.closeElement("div");
    }

    public String jsonSelectFieldOptions() {
        // prepariamo Json
        JsonBuffer jb = new JsonBuffer();

        // apertura array Json
        jb.openArray();

        Map<Object,String> options =
                optionProvider.getOptions(optionProviderIndex);

        if (!options.isEmpty()) {
            jb.openObject();
            jb.writeKeyValue("v", ""); // value
            jb.writeKeyValue("l", comboLabel); // label
            jb.writeKeyValue("s", true); // selected
            jb.closeObject();
        }

        for (Map.Entry<Object,String> current : options.entrySet()) {
            jb.openObject();
            Object value = current.getKey();
            String valueString = Util.convertValueToString(value);
            String label = current.getValue();

            jb.writeKeyValue("v", valueString);
            jb.writeKeyValue("l", label);
            jb.writeKeyValue("s", false);
            jb.closeObject();
        }

        // chiusura array Json
        jb.closeArray();

        return jb.toString();
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    public OptionProvider getOptionProvider() {
        return optionProvider;
    }

    public void setOptionProvider(OptionProvider optionProvider) {
        this.optionProvider = optionProvider;
    }

    public int getOptionProviderIndex() {
        return optionProviderIndex;
    }

    public void setOptionProviderIndex(int optionProviderIndex) {
        this.optionProviderIndex = optionProviderIndex;
    }

    public String getComboLabel() {
        return comboLabel;
    }

    public void setComboLabel(String comboLabel) {
        this.comboLabel = comboLabel;
    }

    public Object getValue() {
        return optionProvider.getValue(optionProviderIndex);
    }
    
    public void setValue(Object value) {
        optionProvider.setValue(optionProviderIndex, value);
    }

    public SelectField getNextSelectField() {
        return nextSelectField;
    }

    public void setNextSelectField(SelectField nextSelectField) {
        this.nextSelectField = nextSelectField;
    }

    public SelectField getPreviousSelectField() {
        return previousSelectField;
    }

    public void setPreviousSelectField(SelectField previousSelectField) {
        this.previousSelectField = previousSelectField;
    }
}
