package gomule.plugy;

import gomule.item.D2Item;
import gomule.util.D2BitReader;

import java.util.ArrayList;
import java.util.List;

public class PlugYSharedStashReader {
    private static final String STASH_FILE_HEADER = "SSS\0";
    private static final String STASH_PAGE_HEADER = "ST";
    private static final String ITEM_LIST_HEADER = "JM";

    public static PlugYSharedStash read(String fileName) {
        D2BitReader d2BitReader = new D2BitReader(fileName);

        String fileHeader = d2BitReader.readBytesAsString(4);
        if (!STASH_FILE_HEADER.equals(fileHeader)) {
            throw new InvalidPlugYSharedStashFileException();
        }

        FileVersion fileVersion = FileVersion.getByCode(d2BitReader.readBytesAsString(2));
        if (fileVersion == FileVersion.UNKNOWN) {
            throw new InvalidPlugYSharedStashFileException();
        }

        Long sharedGolds = null;
        if (fileVersion == FileVersion.V2) {
            sharedGolds = d2BitReader.readBytesAsLong(4);
        }

        Long numberOfPage = d2BitReader.readBytesAsLong(4);
        List<PlugYStashPage> pages = readPages(numberOfPage, d2BitReader);

        return PlugYSharedStash.builder()
                .fileVersion(fileVersion.code)
                .sharedGolds(sharedGolds)
                .numberOfPages(numberOfPage)
                .plugYStashPages(pages)
                .build();
    }

    private static List<PlugYStashPage> readPages(long numberOfPage, D2BitReader d2BitReader) {
        List<PlugYStashPage> pages = new ArrayList<>();
//        for (long i = 0; i < numberOfPage; i++) {
            pages.add(readPage(d2BitReader));
//        }
        return pages;
    }

    private static PlugYStashPage readPage(D2BitReader d2BitReader) {
        String pageHeader = d2BitReader.readBytesAsString(2);
        if (!STASH_PAGE_HEADER.equals(pageHeader)) {
            throw new InvalidPlugYSharedStashFileException();
        }
        // TODO: Flags Data
        String pageName = d2BitReader.readCStyleString();
        if (pageName.length() > 21) {
            throw new InvalidPlugYSharedStashFileException();
        }

        String itemListHeader = d2BitReader.readBytesAsString(2);
        if (!ITEM_LIST_HEADER.equals(itemListHeader)) {
            throw new InvalidPlugYSharedStashFileException();
        }

        int numOfItem = d2BitReader.readBytesAsInteger(2);

        List<D2Item> itemList = new ArrayList<>(numOfItem);
        for (int i = 0; i < numOfItem; i++) {
            try {
                D2Item lItem = new D2Item("", d2BitReader, 75);
                itemList.add(lItem);
            } catch (Exception e) {
                throw new RuntimeException();
            }
        }

        return PlugYStashPage.builder()
                .numberOfItem(numOfItem)
                .stashPageName(pageName)
                .itemList(itemList)
                .build();
    }

    enum FileVersion {
        V1("01"),
        V2("02"),
        UNKNOWN("UNKNOWN");

        FileVersion(String code) {
            this.code = code;
        }

        private String code;

        public static FileVersion getByCode(String code) {
            for (FileVersion value : FileVersion.values()) {
                if (value.code.equals(code)) {
                    return value;
                }
            }
            return UNKNOWN;
        }
    }
}
