package org.apache.fop.fo.flow;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datastructs.TreeException;

public class Dummy {
    public static boolean crash = true;

    private static void setup() {
        try {
            FoBasicLink iFoBasicLink = new FoBasicLink(null, null, null, 0);
            FoBidiOverride iFoBidiOverride = new FoBidiOverride(null, null, null, 0);
            FoBlockContainer iFoBlockContainer = new FoBlockContainer(null, null, null, 0);
            FoBlock iFoBlock = new FoBlock(null, null, null, 0);
            FoCharacter iFoCharacter = new FoCharacter(null, null, null, 0);
            FoExternalGraphic iFoExternalGraphic = new FoExternalGraphic(null, null, null, 0);
            FoFloat iFoFloat = new FoFloat(null, null, null, 0);
            FoFlow iFoFlow = new FoFlow(null, null, null);
            FoFootnoteBody iFoFootnoteBody = new FoFootnoteBody(null, null, null, 0);
            FoFootnote iFoFootnote = new FoFootnote(null, null, null, 0);
            FoInitialPropertySet iFoInitialPropertySet = new FoInitialPropertySet(null, null, null, 0);
            FoInlineContainer iFoInlineContainer = new FoInlineContainer(null, null, null, 0);
            FoInline iFoInline = new FoInline(null, null, null, 0);
            FoInstreamForeignObject iFoInstreamForeignObject = new FoInstreamForeignObject(null, null, null, 0);
            FoLeader iFoLeader = new FoLeader(null, null, null, 0);
            FoListBlock iFoListBlock = new FoListBlock(null, null, null, 0);
            FoListItemBody iFoListItemBody = new FoListItemBody(null, null, null, 0);
            FoListItem iFoListItem = new FoListItem(null, null, null, 0);
            FoListItemLabel iFoListItemLabel = new FoListItemLabel(null, null, null, 0);
            FoMarker iFoMarker = new FoMarker(null, null, null, 0);
            FoMultiCase iFoMultiCase = new FoMultiCase(null, null, null, 0);
            FoMultiProperties iFoMultiProperties = new FoMultiProperties(null, null, null, 0);
            FoMultiPropertySet iFoMultiPropertySet = new FoMultiPropertySet(null, null, null, 0);
            FoMultiSwitch iFoMultiSwitch = new FoMultiSwitch(null, null, null, 0);
            FoMultiToggle iFoMultiToggle = new FoMultiToggle(null, null, null, 0);
            FoNoFo iFoNoFo = new FoNoFo(null, null, null, 0);
            FoPageNumberCitation iFoPageNumberCitation = new FoPageNumberCitation(null, null, null, 0);
            FoPageNumber iFoPageNumber = new FoPageNumber(null, null, null, 0);
            FoPageSequence iFoPageSequence = new FoPageSequence(null, null, null);
            FoPcdata iFoPcdata = new FoPcdata(null, null, null, 0);
            FoRetrieveMarker iFoRetrieveMarker = new FoRetrieveMarker(null, null, null, 0);
            FoStaticContent iFoStaticContent = new FoStaticContent(null, null, null);
            FoTableAndCaption iFoTableAndCaption = new FoTableAndCaption(null, null, null, 0);
            FoTableBody iFoTableBody = new FoTableBody(null, null, null, 0);
            FoTableCaption iFoTableCaption = new FoTableCaption(null, null, null, 0);
            FoTableCell iFoTableCell = new FoTableCell(null, null, null, 0);
            FoTableColumn iFoTableColumn = new FoTableColumn(null, null, null, 0);
            FoTableFooter iFoTableFooter = new FoTableFooter(null, null, null, 0);
            FoTableHeader iFoTableHeader = new FoTableHeader(null, null, null, 0);
            FoTable iFoTable = new FoTable(null, null, null, 0);
            FoTableRow iFoTableRow = new FoTableRow(null, null, null, 0);
            FoTitle iFoTitle = new FoTitle(null, null, null);
            FoWrapper iFoWrapper = new FoWrapper(null, null, null, 0);
        } catch (TreeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FOPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void Main(String[] args) {
        if (crash) throw new RuntimeException("Running Dummy.");
        setup();
    }
}
