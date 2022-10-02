package gomule.plugy;

import gomule.item.D2Item;
import gomule.util.D2BitReader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * @author lousuan
 * This is a PlugY Save Files reader. Currently NOT support any write opeartion.
 */

public class PlugYStashData {
    protected String fileName;
    private List<D2Item> d2ItemList;

    private D2BitReader d2BitReader;
    private long shardGold = 0;
    private boolean isHardCore;
    private boolean isSortCore;

    private int charLevelForProperties = 75; // default char lvl for properties
    private long totalPages = 0;
    private File file;

    private static final String INVALID_SSS_FILE = "invalid sss file";

    public PlugYStashData(String pFileName) throws Exception {
        fileName = pFileName;

        d2ItemList = new ArrayList<>();

        file = new File(pFileName);

//        plugy 好像没办法区分到底是不是 hardcore？
//        iSC = lFile.getName().toLowerCase().startsWith("sc_");
//        iHC = lFile.getName().toLowerCase().startsWith("hc_");
//
        if (!isSortCore && !isHardCore) {
            isSortCore = true;
            isHardCore = true;
        }

        d2BitReader = new D2BitReader(pFileName);

        if (!d2BitReader.isNewFile()) {
            d2BitReader.set_byte_pos(0);
            byte lBytes[] = d2BitReader.get_bytes(3);
            String lStart = new String(lBytes);
//            if ("D2X".equals(lStart)) {
//                readAtmaItems();
//            }
            readItems();
            // clear status
            // 啥意思？
//            setModified(false);
        } else {
            // set status (for first save)
            // 啥意思？
//            setModified(true);
        }
    }

    public long getShardGold() {
        return shardGold;
    }

    public long getTotalPages() {
        return totalPages;
    }

    private void readItems() throws Exception {
        String header = d2BitReader.readFlagString(4);
        if (!"SSS\0".equals(header)) {
            throw new RuntimeException(INVALID_SSS_FILE);
        }

        String version = d2BitReader.readFlagString(2);
        if ("02".equals(version)) {
            this.shardGold = d2BitReader.read(4 * 8);
        }

        totalPages = d2BitReader.read(4 * 8);
        readStashPageList(totalPages);
    }

    private void readStashPageList(long pages) {
        for (int i = 0; i < pages; ++i) {
            int startOfPage = d2BitReader.get_byte_pos();
            String stashHeader = d2BitReader.readFlagString(2);
            if (!"ST".equals(stashHeader)) {
                throw new RuntimeException(INVALID_SSS_FILE);
            }

            int currentPosition = d2BitReader.get_byte_pos();
            int posOfNextNullCharacter = d2BitReader.findNextFlag("\0", currentPosition);
            int posOfEndOfPageName = d2BitReader.findNextFlag("\0JM", currentPosition);
            if (posOfNextNullCharacter != posOfEndOfPageName) {
                // TODO: handle flags?
            }
            int startOfListData = posOfEndOfPageName + 1;
            d2BitReader.set_byte_pos(startOfListData);

            try {
                readItemList();
            } catch (Exception e) {
                System.out.println("读取文件失败: " + e.getStackTrace());
            }
        }
        System.out.println("读取完成！");
    }

    private void readItemList() throws Exception {
        String header = d2BitReader.readFlagString(2);
        if (!"JM".equals(header)) {
            throw new RuntimeException(INVALID_SSS_FILE);
        }
        long numberOfItems = d2BitReader.read(2 * 8);
        for (int i = 0; i < numberOfItems; i++) {
            D2Item lItem = new D2Item(fileName, d2BitReader, d2BitReader.get_byte_pos(), charLevelForProperties);
            d2ItemList.add(lItem);
        }
    }

    public void convertToD2s(String newFileName) {
        // backup file
        D2BitReader iBR = new D2BitReader(newFileName);

        int size = 0;
        List<D2Item> iItems = d2ItemList;
        for (D2Item iItem : iItems) {
            size += iItem.get_bytes().length;
        }
        byte[] newbytes = new byte[size + 11];
        newbytes[0] = 'D';
        newbytes[1] = '2';
        newbytes[2] = 'X';
        int pos = 11;
        for (D2Item iItem : iItems) {
            byte[] itemBytes = iItem.get_bytes();
            for (int j = 0; j < itemBytes.length; j++) {
                newbytes[pos++] = itemBytes[j];
            }
        }

        iBR.setBytes(newbytes);

        iBR.set_byte_pos(3);
        iBR.write(iItems.size(), 16);
        iBR.write(96, 16); // version 96
//        iBR.replace_bytes(11, iBR.get_length(), newbytes);

        long lCheckSum1 = calculateAtmaCheckSum(iBR);
//        System.err.println("CheckSum at saving: " + lCheckSum1 );

        iBR.set_byte_pos(7);
        iBR.write(lCheckSum1, 32);

        iBR.set_byte_pos(7);
        long lCheckSum2 = iBR.read(32);

//        long lCheckSum3 = calculateGoMuleCheckSum();
//        System.err.println("CheckSum after insert: " + lCheckSum3 );

        if (lCheckSum1 == lCheckSum2) {
            iBR.save();
        } else {
            System.err.println("Incorrect CheckSum");
        }
    }

    private long calculateAtmaCheckSum(D2BitReader iBR) {
        long lCheckSum;
        lCheckSum = 0;


        iBR.set_byte_pos(0);
        // calculate a new checksum
        for (int i = 0; i < iBR.get_length(); i++) {
            long lByte = iBR.read(8);
            if (i >= 7 && i <= 10) {
                lByte = 0;
            }

            long upshift = lCheckSum << 33 >>> 32;
            long add = lByte + ((lCheckSum >>> 31) == 1 ? 1 : 0);
            lCheckSum = upshift + add;
        }

//		System.err.println("Test " + lOriginal + " - " + lCheckSum + " = " + (lOriginal == lCheckSum) );
        return lCheckSum;
    }
}
