package gomule.plugy;

import lombok.Builder;
import lombok.Getter;

import java.io.File;
import java.util.List;

@Builder
@Getter
public class PlugYSharedStash {
    // should I move this field to reader?
    private File file;
    private String fileVersion;
    private Long sharedGolds;
    private Long numberOfPages;
    private List<PlugYStashPage> plugYStashPages;

    /**
     * @param pageIndex start from 1
     * @return corresponding page
     */
    public PlugYStashPage getPage(int pageIndex) {
        if (pageIndex > numberOfPages) {
            throw new RuntimeException();
        }
        return plugYStashPages.get(pageIndex - 1);
    }
}
