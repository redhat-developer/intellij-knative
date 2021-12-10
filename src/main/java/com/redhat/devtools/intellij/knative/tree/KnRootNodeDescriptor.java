package com.redhat.devtools.intellij.knative.tree;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.List;

public class KnRootNodeDescriptor extends LabelAndIconDescriptor<KnRootNode> {
    private static final SimpleTextAttributes WARNING_ATTRIBUTES = new SimpleTextAttributes(SimpleTextAttributes.STYLE_WAVED, null, JBColor.RED);

    private final KnRootNode element;

    public KnRootNodeDescriptor(Project project, KnRootNode element, String label, Icon nodeIcon, @Nullable NodeDescriptor parentDescriptor) {
        super(project, element, label, nodeIcon, parentDescriptor);
        this.element = element;
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
        super.update(presentation);
        element.setConsumerWarnings((warnings) -> showWarning(warnings, presentation));
    }

    public void showWarning(List<String> warnings, @NotNull PresentationData presentation) {
        String presentableText = presentation.getPresentableText();
        presentation.setPresentableText("");
        if (!warnings.isEmpty()) {
            presentation.addText(presentableText, WARNING_ATTRIBUTES);
            presentation.setTooltip(String.join("\n", warnings));
        } else {
            presentation.addText(presentableText, SimpleTextAttributes.REGULAR_ATTRIBUTES);
            presentation.setTooltip("");
        }
    }
}
