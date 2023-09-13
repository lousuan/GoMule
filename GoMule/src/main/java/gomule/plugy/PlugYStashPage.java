package gomule.plugy;

import gomule.item.D2Item;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class PlugYStashPage {
    private String stashPageName;
    private Integer numberOfItem;
    private List<D2Item> itemList;

    private boolean stashPageNameIsValid() {
        return stashPageName == null || stashPageName.length() <= 15;
    }


}
