package minecrafttransportsimulator.items.components;

import java.util.Collections;
import java.util.List;

import minecrafttransportsimulator.jsondefs.AJSONMultiModelProvider;
import minecrafttransportsimulator.jsondefs.JSONSubDefinition;
import minecrafttransportsimulator.mcinterface.IWrapperNBT;

public abstract class AItemSubTyped<JSONDefinition extends AJSONMultiModelProvider> extends AItemPack<JSONDefinition> {
    public final String subName;

    public AItemSubTyped(JSONDefinition definition, String subName, String sourcePackID) {
        super(definition, sourcePackID);
        this.subName = subName;
    }

    @Override
    public String getRegistrationName() {
        return super.getRegistrationName() + subName;
    }

    @Override
    public void addTooltipLines(List<String> tooltipLines, IWrapperNBT data) {
        super.addTooltipLines(tooltipLines, data);
        for (JSONSubDefinition subDefinition : definition.definitions) {
            if (subDefinition.subName.equals(subName)) {
                if (subDefinition.description != null) {
                    Collections.addAll(tooltipLines, subDefinition.description.split("\n"));
                }
            }
        }
    }

    @Override
    public void populateDefaultData(IWrapperNBT data) {
        super.populateDefaultData(data);
        data.setString("subName", subName);
    }

    public List<List<String>> getExtraMaterials() {
        for (JSONSubDefinition subDefinition : definition.definitions) {
            if (subDefinition.subName.equals(subName)) {
                return subDefinition.extraMaterialLists;
            }
        }
        return null;
    }
}