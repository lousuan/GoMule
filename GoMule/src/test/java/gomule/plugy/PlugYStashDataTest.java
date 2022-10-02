package gomule.plugy;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import randall.d2files.D2TblFile;
import randall.d2files.D2TxtFile;

public class PlugYStashDataTest {

    @Test
    public void testConstructor() throws Exception {
        D2TxtFile.constructTxtFiles("./d2111");
        D2TblFile.readAllFiles("./d2111");


        PlugYStashData plugYStashData = new PlugYStashData("/home/lousuan/下载/_LOD_SharedStashSave.sss");
        Assertions.assertEquals(26283667L, plugYStashData.getShardGold());
        Assertions.assertEquals(103L, plugYStashData.getTotalPages());

        plugYStashData.convertToD2s("/home/lousuan/workspace/java/GoMule/testSave/aaa.d2x");
    }
}
