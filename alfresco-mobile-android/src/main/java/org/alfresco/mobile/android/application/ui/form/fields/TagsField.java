
package org.alfresco.mobile.android.application.ui.form.fields;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.Property;
import org.alfresco.mobile.android.api.model.PropertyDefinition;
import org.alfresco.mobile.android.api.model.Tag;
import org.alfresco.mobile.android.api.model.config.FieldConfig;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.ui.form.FormManager;
import org.alfresco.mobile.android.application.ui.widget.TagsSpanRenderer;
import org.alfresco.mobile.android.async.OperationEvent;
import org.alfresco.mobile.android.async.OperationRequest;
import org.alfresco.mobile.android.async.tag.TagsEvent;
import org.alfresco.mobile.android.async.tag.TagsOperationRequest;
import org.alfresco.mobile.android.ui.holder.HolderUtils;
import org.alfresco.mobile.android.ui.holder.TwoLinesViewHolder;

import android.content.Context;
import android.view.View;

import com.jmpergar.awesometext.AwesomeTextHandler;

/**
 * Created by jpascal on 28/03/2015.
 */
public class TagsField extends BaseField
{
    protected TwoLinesViewHolder vh;

    private static final String COMMA_PATTERN = "(¤[\\p{L}0-9-_ #$;,^\\*\\.]+¤¤)";

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public TagsField(Context context, FormManager manager, Property property, FieldConfig configuration,
            PropertyDefinition propertyDefinition, boolean isReadMode)
    {
        super(context, manager, property, configuration, propertyDefinition, isReadMode);
    }

    public View setupdReadView()
    {
        View vr = inflater.inflate(R.layout.form_read_row, null);
        vr.setFocusable(false);
        vh = HolderUtils.configure(vr, fieldConfig.getLabel(), getHumanReadableReadValue(), -1);
        vh.topText.setVisibility(View.GONE);

        readView = vr;
        return vr;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // VALUES
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Object getEditionValue()
    {
        return null;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // VIEW GENERATOR
    // ///////////////////////////////////////////////////////////////////////////
    public View setupEditionView(Object value)
    {
        return null;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // REQUIRE REST API
    // ///////////////////////////////////////////////////////////////////////////
    public boolean requireAsync()
    {
        return true;
    }

    public OperationRequest.OperationBuilder requestData(Object extra)
    {
        return new TagsOperationRequest.Builder((Node) extra);
    }

    public void setOperationData(OperationEvent event)
    {
        if (event instanceof TagsEvent)
        {
            PagingResult<Tag> result = ((TagsEvent) event).data;

            // No Tags but taggable aspect present
            // Hide tags group
            /*
             * if (result.getTotalItems() == 0) { ((ViewGroup)
             * tv.getParent().getParent().getParent()).setVisibility(View.GONE);
             * return; }
             */

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < result.getTotalItems(); i++)
            {
                builder.append(" ¤").append(result.getList().get(i).getValue()).append("¤¤ ");
            }
            vh.bottomText.setText(builder.toString());

            AwesomeTextHandler awesomeTextViewHandler = new AwesomeTextHandler();
            awesomeTextViewHandler.addViewSpanRenderer(COMMA_PATTERN, new TagsSpanRenderer()).setView(vh.bottomText);
        }
    }

}
