package net.minecraft.client.gui.screens.reporting;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.PlayerSkinWidget;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.report.ReportReason;
import net.minecraft.client.multiplayer.chat.report.ReportType;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.multiplayer.chat.report.SkinReport;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkinReportScreen extends AbstractReportScreen<SkinReport.Builder> {
    private static final int SKIN_WIDTH = 85;
    private static final int FORM_WIDTH = 178;
    private static final Component TITLE = Component.translatable("gui.abuseReport.skin.title");
    private MultiLineEditBox commentBox;
    private Button selectReasonButton;

    private SkinReportScreen(Screen p_297956_, ReportingContext p_297244_, SkinReport.Builder p_297725_) {
        super(TITLE, p_297956_, p_297244_, p_297725_);
    }

    public SkinReportScreen(Screen p_297357_, ReportingContext p_300387_, UUID p_300713_, Supplier<PlayerSkin> p_299154_) {
        this(p_297357_, p_300387_, new SkinReport.Builder(p_300713_, p_299154_, p_300387_.sender().reportLimits()));
    }

    public SkinReportScreen(Screen p_300918_, ReportingContext p_298862_, SkinReport p_299017_) {
        this(p_300918_, p_298862_, new SkinReport.Builder(p_299017_, p_298862_.sender().reportLimits()));
    }

    @Override
    protected void addContent() {
        LinearLayout linearlayout = this.layout.addChild(LinearLayout.horizontal().spacing(8));
        linearlayout.defaultCellSetting().alignVerticallyMiddle();
        linearlayout.addChild(new PlayerSkinWidget(85, 120, this.minecraft.getEntityModels(), this.reportBuilder.report().getSkinGetter()));
        LinearLayout linearlayout1 = linearlayout.addChild(LinearLayout.vertical().spacing(8));
        this.selectReasonButton = Button.builder(
                SELECT_REASON,
                p_357696_ -> this.minecraft.setScreen(new ReportReasonSelectionScreen(this, this.reportBuilder.reason(), ReportType.SKIN, p_299180_ -> {
                        this.reportBuilder.setReason(p_299180_);
                        this.onReportChanged();
                    }))
            )
            .width(178)
            .build();
        linearlayout1.addChild(CommonLayouts.labeledElement(this.font, this.selectReasonButton, OBSERVED_WHAT_LABEL));
        this.commentBox = this.createCommentBox(178, 9 * 8, p_300794_ -> {
            this.reportBuilder.setComments(p_300794_);
            this.onReportChanged();
        });
        linearlayout1.addChild(CommonLayouts.labeledElement(this.font, this.commentBox, MORE_COMMENTS_LABEL, p_299506_ -> p_299506_.paddingBottom(12)));
    }

    @Override
    protected void onReportChanged() {
        ReportReason reportreason = this.reportBuilder.reason();
        if (reportreason != null) {
            this.selectReasonButton.setMessage(reportreason.title());
        } else {
            this.selectReasonButton.setMessage(SELECT_REASON);
        }

        super.onReportChanged();
    }

    @Override
    public boolean mouseReleased(double p_298823_, double p_297602_, int p_299980_) {
        return super.mouseReleased(p_298823_, p_297602_, p_299980_) ? true : this.commentBox.mouseReleased(p_298823_, p_297602_, p_299980_);
    }
}