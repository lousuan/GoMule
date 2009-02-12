/*******************************************************************************
 * 
 * Copyright 2007 Andy Theuninck, Randall & Silospen
 * 
 * This file is part of gomule.
 * 
 * gomule is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * gomule is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * gomlue; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 *  
 ******************************************************************************/

package gomule.item;

import gomule.gui.*;
import gomule.gui.D2ViewChar.*;
import gomule.item.D2ItemProperty.PropValue;
import gomule.util.*;

import java.awt.Color;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import randall.d2files.*;
import randall.flavie.*;

//an item class
//manages one item
//keeps the a copy of the bytes representing
//an item and a bitreader to manipulate them
//also stores most data from the item in

//this class is NOT designed to edit items
//any methods that allow the item's bytes
//to be written only exist to facillitate
//moving items. writing other item fields
//is not supported by this class
public class D2Item implements Comparable, D2ItemInterface {
//	private ArrayList iProperties = new ArrayList();

	private D2PropCollection iProps = new D2PropCollection();

	private ArrayList iSet1 = new ArrayList();

	private ArrayList iSet2 = new ArrayList();

	private ArrayList iSet3 = new ArrayList();

	private ArrayList iSet4 = new ArrayList();

	private ArrayList iSet5 = new ArrayList();

	private ArrayList iSetProps;

	private ArrayList iSocketedItems;

	private ArrayList iRuneWordProps;

	// general item data
	private int flags;

	private short version;

	private String uberCode;

	private short location;

	private short body_position;

	private short row;

	private short col;

	private short panel;

	private String item_type;

	// additional data for complex items
	private long iSocketNrFilled = 0;

	private long iSocketNrTotal = 0;

	private long fingerprint;

	private short ilvl;

	private short quality;

	private short gfx_num;

	private D2TxtFileItemProperties automod_info;

	private short low_quality;

	private short hi_quality;

	private short magic_prefix;

	private short magic_suffix;

	private short set_id;

	private short rare_name_1;

	private short rare_name_2;

	private short[] rare_prefixes;

	private short[] rare_suffixes;

	private short unique_id;

	private int runeword_id;

	private String personalization;

	private short tome;

	// useful item data to keep on hand
	// that's in txt files rather than
	// in the character file
	private short width;

	private short height;

	private String image_file;

	private String name;

	private D2TxtFileItemProperties iItemType;

	private String iType;

	private String iType2;

	private boolean iEthereal;

	private boolean iQuest;

	private boolean iSocketed;

	private boolean iThrow;

	private boolean iMagical;

	private boolean iRare;

	private boolean iCrafted;

	private boolean iSet;

	private boolean iUnique;

	private boolean iRuneWord;

	private boolean iSmallCharm;

	private boolean iLargeCharm;

	private boolean iGrandCharm;

	private boolean iJewel;

	private boolean iEquipped = false;

	private boolean iGem;

	private boolean iStackable = false;

	private ArrayList iGemProps;

	private boolean iRune;

	private boolean iTypeMisc;

	private boolean iIdentified;

	private boolean iTypeWeapon;

	private boolean iTypeArmor;

	protected String iItemName;

	protected String iBaseItemName;

	protected String iItemNameNoPersonalising;

	private String iItemNameWithSockets;

	private long iCurDur;

	private long iMaxDur;

	private long iDef;

	private long cBlock;

	private long iBlock;

	private long iInitDef;

	private long iMinDmg;

	private long iMaxDmg;

	// BARBARIANS
	private long i2MinDmg;

	private long i2MaxDmg;

	private long iinitMinDmg;

	private long iinitMaxDmg;

	// BARBARIANS
	private long iinit2MinDmg;

	private long iinit2MaxDmg;

	// 0 FOR BOTH 1 FOR 1H 2 FOR 2H
	private int iWhichHand;

	// private int iLvl;
	private String iFP;

	private boolean hasGUID;

	private String iGUID;

	private boolean iBody = false;

	private String iBodyLoc1 = null;

	private String iBodyLoc2 = null;

	private boolean iBelt = false;

	private D2BitReader iItem;

	private String iFileName;

	private boolean iIsChar;

	private long iCharLvl;

	private int iReqLvl = -1;

	private int iReqStr = -1;

	private int iReqDex = -1;

	private long lSet1;

	private long lSet2;

	private long lSet3;

	private long lSet4;

	private long lSet5;

	private String iSetName;

	private int setSize;

	private String iItemQuality = "none";

	// private int iPossibleItemLength = 0;

	public D2Item(String pFileName, D2BitReader pFile, int pPos, long pCharLvl)
	throws Exception {
		boolean end = false;
		iFileName = pFileName;
		iIsChar = iFileName.endsWith(".d2s");
		iCharLvl = pCharLvl;
		try {
			// bytedata = b;
			// br = new D2BitReader(bytedata);
			pFile.set_byte_pos(pPos);
			read_item(pFile, pPos);
			// pFile.set_byte_pos(pPos);
			int lCurrentReadLength = pFile.get_pos() - pPos * 8;
			int lNextJMPos = pFile.findNextFlag("JM", pFile.get_byte_pos());
			int lLengthToNextJM = lNextJMPos - pPos;

			if (lLengthToNextJM < 0) {
				int lNextKFPos = pFile.findNextFlag("kf", pFile.get_byte_pos());
				int lNextJFPos = pFile.findNextFlag("jf", pFile.get_byte_pos());
				if (lNextJFPos >= 0) {

					lLengthToNextJM = lNextJFPos - pPos;

				} else if (lNextKFPos >= 0) {
					lLengthToNextJM = lNextKFPos - pPos;
				}

				else {
					// last item (for stash only)
					lLengthToNextJM = pFile.get_length() - pPos;
				}
			} else if ((lNextJMPos > pFile.findNextFlag("kf", pFile
					.get_byte_pos()))
					&& (pPos < pFile.findNextFlag("kf", pFile.get_byte_pos()))) {
				lLengthToNextJM = pFile
				.findNextFlag("kf", pFile.get_byte_pos())
				- pPos;
			} else if ((lNextJMPos > pFile.findNextFlag("jf", pFile
					.get_byte_pos()))
					&& (pPos < pFile.findNextFlag("jf", pFile.get_byte_pos()))) {

				lLengthToNextJM = pFile
				.findNextFlag("jf", pFile.get_byte_pos())
				- pPos;

			}

			// pFile.findNextFlag("kf", pFile.get_byte_pos()) - pPos;
			int lDiff = ((lLengthToNextJM * 8) - lCurrentReadLength);
			if (lDiff > 7) {
				throw new D2ItemException(
						"Item not read complete, missing bits: " + lDiff
						+ getExStr());
				// System.out.println("GRUMBLE GRUMBLE");
				// System.err.println("Test: " + lCurrentReadLength + " - " +
				// lLengthToNextJM*8 + ": " + lDiff);
			}

			// if (end) {
			// return;
			// }

			// System.err.println("Current read length: " + lCurrentReadLength +
			// " - " + lLengthToNextJM );

			pFile.set_byte_pos(pPos);
			iItem = new D2BitReader(pFile.get_bytes(lLengthToNextJM));
			pFile.set_byte_pos(pPos + lLengthToNextJM);
		} catch (D2ItemException pEx) {
			throw pEx;
		} catch (Exception pEx) {
			pEx.printStackTrace();
			throw new D2ItemException("Error: " + pEx.getMessage() + getExStr());
		}

//		if(item_type != null && isTypeArmor()){
//		System.out.println(Integer.parseInt(D2TxtFile.ARMOR.searchColumns("code", item_type).get("belt")));
//		System.err.println("Test: " + getItemName() + " - " + getItemLength()
//		);
//		}

	}

	public String getFileName() {
		return iFileName;
	}

	public boolean isCharacter() {
		return iIsChar;
	}

	// bit checker
	// if the specified bit of flags is set,
	// return true
	private boolean check_flag(int bit) {
		if (((flags >>> (32 - bit)) & 1) == 1)
			return true;
		else
			return false;
	}

	// read basic information from the bytes
	// common to all items, then split based on
	// whether the item is an ear
	private void read_item(D2BitReader pFile, int pos) throws Exception {
		pFile.skipBytes(2);
		flags = (int) pFile.unflip(pFile.read(32), 32); // 4 bytes

		iSocketed = check_flag(12); // 12
		iEthereal = check_flag(23); // 23
		iRuneWord = check_flag(27); // 27
		iIdentified = check_flag(5);

		version = (short) pFile.read(8); // 1 byte

		pFile.skipBits(2);
		location = (short) pFile.read(3);
		body_position = (short) pFile.read(4);
		col = (short) pFile.read(4);
		row = (short) pFile.read(4);
		panel = (short) pFile.read(3); // 20 bits -> 2,5 byte

		// flag 17 is an ear
		if (!check_flag(17)) {

			readExtend(pFile);
		} else {
			read_ear(pFile);
		}
		if (personalization == null) {
			iItemNameNoPersonalising = iItemName;
		}


//		applyReqLPlus();

		if (isTypeWeapon()) {

			if (iEthereal) {
				applyEthDmg();
			}

//			if (iType.equals("club") || iType.equals("scep")
//					|| iType.equals("mace") || iType.equals("hamm"))
//
//			{
//				D2ItemProperty lProperty = new D2ItemProperty(122, iCharLvl,
//						iItemName);
//				D2TxtFileItemProperties lItemStatCost2 = D2TxtFile.ITEM_STAT_COST
//				.getRow(122);
//				lProperty.set(122, lItemStatCost2, 0, 150);
//				iProperties.add(lProperty);
//			}

			if (isSocketed()) {
//				combineProps();
			}
//			combineResists();
//			applyEDmg();
			
//			ArrayList lvlSkills = new ArrayList();
			
//			for (int x = 0; x < iProperties.size(); x = x + 1) {
//				if (((D2ItemProperty) iProperties.get(x)).getiProp() == 107
//						|| ((D2ItemProperty) iProperties.get(x)).getiProp() == 97) {
//					lvlSkills.add(iProperties.get(x));
//				}
//			}
//			if (lvlSkills.size() > 0) {
//				modifyLvl(lvlSkills);
//			}
			

		} else if (isTypeArmor()) {



			if (isSocketed()) {

//				combineProps();

			}
//			combineResists();
//			applyEDef();

			if(iType.equals("ashd")||iType.equals("shie")||iType.equals("head")){

//				applyBlock();	

			}

//			ArrayList lvlSkills = new ArrayList();
//			for (int x = 0; x < iProperties.size(); x = x + 1) {
//				if (((D2ItemProperty) iProperties.get(x)).getiProp() == 107
//						|| ((D2ItemProperty) iProperties.get(x)).getiProp() == 97) {
//					lvlSkills.add(iProperties.get(x));
//				}
//			}
//			if (lvlSkills.size() > 0) {
//				modifyLvl(lvlSkills);
//			}

		} else {
			if (isRare() || isCrafted()) {
//				combineProps();
//				combineResists();
			}
		}


	}

//	private void applyReqLPlus() {
//
//		for (int x = 0; x < iProperties.size(); x = x + 1) {
//			if((((D2ItemProperty) iProperties.get(x)).getiProp())==92){
//				iReqLvl=iReqLvl+ ((D2ItemProperty) iProperties.get(x)).getRealValue();
//			}
//
//		}
//
//	}

	// read ear related data from the bytes
	private void read_ear(D2BitReader pFile) { // br.getFileContent()

		// getCharacterCode((int)
		// Ear Class TRANSLATE TO CLASS! CORRECT
		int eClass = (int) pFile.read(3);

		int eLevel = (int) (pFile.read(7)); // Ear Lvl CORRECT

		StringBuffer lCharName = new StringBuffer();
		for (int i = 0; i < 18; i++)
			// while(1==1)
		{
			long lChar = pFile.read(7);
			if (lChar != 0) {
				lCharName.append((char) lChar);
			} else {
				break;
			}
		}
		iItemType = D2TxtFile.search("ear");
		height = Short.parseShort(iItemType.get("invheight"));
		width = Short.parseShort(iItemType.get("invwidth"));
		image_file = iItemType.get("invfile");
		name = iItemType.get("name");
		iBaseItemName = iItemName = lCharName.toString() + "'s Ear";

		D2ItemProperty lProperty = new D2ItemProperty(1340, iCharLvl, iItemName);
		lProperty.set(1340, new D2TxtFileItemProperties(null, 0), 0, eClass);
		lProperty.set(1340, new D2TxtFileItemProperties(null, 0), 0, eLevel);
//		iProperties.add(lProperty);

		// iProperties.add(arg0)
		// System.out.println(lCharName.toString());

		// for (int i = 0; i < 18; i++) {
		// pFile.read(7); // name
		// }
	}

	// read non ear data from the bytes,
	// setting class variables for easier access
	private void readExtend(D2BitReader pFile) throws Exception {
		// 9,5 bytes already read (common data)
		item_type = "";
		// skip spaces or hashing won't work
		for (int i = 0; i < 4; i++) {
			char c = (char) pFile.read(8); // 4 bytes
			if (c != 32) {
				item_type += c;
			}
		}

		iItemType = D2TxtFile.search(item_type);
		height = Short.parseShort(iItemType.get("invheight"));
		width = Short.parseShort(iItemType.get("invwidth"));
		image_file = iItemType.get("invfile");
		name = iItemType.get("name");

		String lD2TxtFileName = iItemType.getFileName();
		if (lD2TxtFileName != null) {
			iTypeMisc = ("Misc".equals(lD2TxtFileName));
			iTypeWeapon = ("weapons".equals(lD2TxtFileName));
			iTypeArmor = ("armor".equals(lD2TxtFileName));
		}

		iType = iItemType.get("type");
		iType2 = iItemType.get("type2");


		//Shields - block chance.
		if(iType.equals("ashd")||iType.equals("shie")||iType.equals("head")){
			cBlock = Long.parseLong(iItemType.get("block"));
//			System.out.println(cBlock);
		}

		// Requerements
		if (iTypeMisc) {
			iReqLvl = getReq(iItemType.get("levelreq"));
		} else if (iTypeArmor) {
			iReqLvl = getReq(iItemType.get("levelreq"));
			iReqStr = getReq(iItemType.get("reqstr"));

			D2TxtFileItemProperties qualSearch = D2TxtFile.ARMOR.searchColumns(
					"normcode", item_type);
			iItemQuality = "normal";
			if (qualSearch == null) {
				qualSearch = D2TxtFile.ARMOR.searchColumns("ubercode",
						item_type);
				iItemQuality = "exceptional";
				if (qualSearch == null) {
					qualSearch = D2TxtFile.ARMOR.searchColumns("ultracode",
							item_type);
					iItemQuality = "elite";
				}
			}

		} else if (iTypeWeapon) {
			iReqLvl = getReq(iItemType.get("levelreq"));
			iReqStr = getReq(iItemType.get("reqstr"));
			iReqDex = getReq(iItemType.get("reqdex"));

			D2TxtFileItemProperties qualSearch = D2TxtFile.WEAPONS
			.searchColumns("normcode", item_type);
			iItemQuality = "normal";
			if (qualSearch == null) {
				qualSearch = D2TxtFile.WEAPONS.searchColumns("ubercode",
						item_type);
				iItemQuality = "exceptional";
				if (qualSearch == null) {
					qualSearch = D2TxtFile.WEAPONS.searchColumns("ultracode",
							item_type);
					iItemQuality = "elite";
				}
			}

			// System.err.println("Weapon: " + item_type + " - " +
			// iItemType.get("type") + " - " + iItemType.get("type2") + " - " +
			// iItemType.get("code") );
		}

		String lItemName = D2TblFile.getString(item_type);
		if (lItemName != null) {
			iItemName = lItemName;
			iBaseItemName = iItemName;
			iItemNameWithSockets = iItemName;
		}

		// flag 22 is a simple item (extend1)
		if (!check_flag(22)) {
			readExtend1(pFile);
		}

		// gold (?)
		if ("gold".equals(item_type)) {
			if (pFile.read(1) == 0) {
				pFile.read(12);
			} else {
				pFile.read(32);
			}
		}

		long lHasRand = pFile.read(1);

		if (lHasRand == 1) { // GUID ???
			// if (!check_flag(22)) {
			if (iType.startsWith("rune") || iType.startsWith("gem")
					|| iType.startsWith("amu") || iType.startsWith("rin")
					|| !isTypeMisc()) {
				hasGUID = true;

				iGUID = "0x" + Integer.toHexString((int) pFile.read(32))
				+ " 0x" + Integer.toHexString((int) pFile.read(32))
				+ " 0x" + Integer.toHexString((int) pFile.read(32));
				// }
				// pFile.read(32);
			} else {
				pFile.read(3);
			}
		}

		// flag 22 is a simple item (extend2)
		if (!check_flag(22)) {
			readExtend2(pFile);
		}

		if (iType != null && iType2 != null && iType.startsWith("gem")) {
			if (iType2.equals("gem0") || iType2.equals("gem1")
					|| iType2.equals("gem2") || iType2.equals("gem3")
					|| iType2.equals("gem4")) {
				iGemProps = new ArrayList();
				readPropertiesGems(pFile, iGemProps);

				iGem = true;
			}
		}

		if (iType != null && iType2 != null && iType.startsWith("rune")) {
			iGemProps = new ArrayList();
			readPropertiesRunes(pFile, iGemProps);
			iRune = true;

		}

		D2TxtFileItemProperties lItemType = D2TxtFile.ITEM_TYPES.searchColumns(
				"Code", iType);

		if (lItemType == null) {
			lItemType = D2TxtFile.ITEM_TYPES.searchColumns("Equiv1", iType);
			if (lItemType == null) {
				lItemType = D2TxtFile.ITEM_TYPES.searchColumns("Equiv2", iType);
			}
		}

		if ("1".equals(lItemType.get("Body"))) {
			iBody = true;
			iBodyLoc1 = lItemType.get("BodyLoc1");
			iBodyLoc2 = lItemType.get("BodyLoc2");
		}
		if ("1".equals(lItemType.get("Beltable"))) {
			iBelt = true;

			readPropertiesPots(pFile);
		}

		int lLastItem = pFile.get_byte_pos();
		if (iSocketNrFilled > 0) {
			iGemProps = new ArrayList();
			iSocketedItems = new ArrayList();
			for (int i = 0; i < iSocketNrFilled; i++) {
				int lStartNewItem = pFile.findNextFlag("JM", lLastItem);
				D2Item lSocket = new D2Item(iFileName, pFile, lStartNewItem,
						iCharLvl);
				lLastItem = lStartNewItem + lSocket.getItemLength();
				iSocketedItems.add(lSocket);
				if (!lSocket.isJewel()) {
					if (isTypeWeapon()) {
						iGemProps.addAll((ArrayList) lSocket.iGemProps.get(0));
					} else if (isTypeArmor()) {
						if (iType.equals("tors") || iType.equals("helm")
								|| iType.equals("phlm") || iType.equals("pelt")
								|| iType.equals("cloa") || iType.equals("circ")) {
							iGemProps.addAll((ArrayList) lSocket.iGemProps
									.get(1));
						} else {
							iGemProps.addAll((ArrayList) lSocket.iGemProps
									.get(2));
						}
					}
				} else {
//					iGemProps.addAll((ArrayList) lSocket.iProperties);
				}
				if (lSocket.iReqLvl > iReqLvl) {
					iReqLvl = lSocket.iReqLvl;
				}
			}
		}

		if (iRuneWord) {
			ArrayList lList = new ArrayList();
			for (int i = 0; i < iSocketedItems.size(); i++) {
				lList.add(((D2Item) iSocketedItems.get(i)).getRuneCode());
			}

			D2TxtFileItemProperties lRuneWord = D2TxtFile.RUNES
			.searchRuneWord(lList);
			runeword_id = lRuneWord.getRowNum();
			if (lRuneWord != null) {
				// iItem += " Runeword found " + D2TblFile.getString(
				// lRuneWord.get("Name") ) + " - " + D2TblFile.getString(
				// lRuneWord.get("Rune Name") ) + "\n";
				iItemName = D2TblFile.getString(lRuneWord.get("Name"));
				// readProperties(pFile, iProperties);
				// if ( iItemName == null )
				// {
				// System.err.println("Runeword: " + iItemName );
				// }
			}
		}

		if (iSocketNrFilled > 0 && isNormal()) {
			iItemName = "Gemmed " + iItemName;
		}

		if (iItemName != null) {
			iItemName = iItemName.trim();

		}

		if (iBaseItemName != null) {
			iBaseItemName = iBaseItemName.trim();

		}

		if (iEthereal) {
			if (iReqStr != -1) {
				iReqStr -= 10;
			}
			if (iReqDex != -1) {
				iReqDex -= 10;
			}
		}
	}

	public String getItemQuality() {
		return iItemQuality;
	}

	private int getReq(String pReq) {
		if (pReq != null) {
			String lReq = pReq.trim();
			if (!lReq.equals("") && !lReq.equals("0")) {
				try {
					return Integer.parseInt(lReq);
				} catch (Exception pEx) {
					// do nothing, no req
				}
			}
		}
		return -1;
	}

	private void readExtend1(D2BitReader pFile) throws Exception {
		// extended item
		iSocketNrFilled = (short) pFile.read(3);
		fingerprint = pFile.read(32);
		iFP = "0x" + Integer.toHexString((int) fingerprint);
		ilvl = (short) pFile.read(7);
		quality = (short) pFile.read(4);
		// check variable graphic flag
		gfx_num = -1;
		if (pFile.read(1) == 1) {
			gfx_num = (short) pFile.read(3);
			if (iItemType.get("namestr").compareTo("cm1") == 0) {
				iSmallCharm = true;
				image_file = "invch" + ((gfx_num) * 3 + 1);
			} else if (iItemType.get("namestr").compareTo("cm2") == 0) {
				iLargeCharm = true;
				image_file = "invch" + ((gfx_num) * 3 + 2);
			} else if (iItemType.get("namestr").compareTo("cm3") == 0) {
				iGrandCharm = true;
				image_file = "invch" + ((gfx_num) * 3 + 3);
			} else if (iItemType.get("namestr").compareTo("jew") == 0) {
				iJewel = true;
				image_file = "invjw" + (gfx_num + 1);
			} else {
				image_file += (gfx_num + 1);
			}
		}
		// check class info flag
		if (pFile.read(1) == 1) {
			automod_info = D2TxtFile.AUTOMAGIC.getRow((int)pFile.read(11) -1);
//			automod_info = D2TxtFile.AUTOMAGIC.getRow(0);
//			System.out.println(automod_info.get("Name"));


		}

		// path determined by item quality
		switch (quality) {
		case 1: // low quality item
		{
			low_quality = (short) pFile.read(3);

			switch(low_quality){

			case 0:
			{
				iItemName = "Crude " + iItemName;
				break;
			}

			case 1:
			{
				iItemName = "Cracked " + iItemName;
				break;
			}

			case 2:
			{
				iItemName = "Damaged " + iItemName;
				break;
			}

			case 3:
			{
				iItemName = "Low Quality " + iItemName;
				break;
			}

			}


			break;
		}
		case 3: // high quality item
		{
			iItemName = "Superior " + iItemName;
			iBaseItemName = iItemName;
			hi_quality = (short) pFile.read(3);
			break;
		}
		case 4: // magic item
		{
			iMagical = true;
			magic_prefix = (short) pFile.read(11);
			magic_suffix = (short) pFile.read(11);

			if (magic_suffix == 0) {
				magic_suffix = 10000;
			}

			D2TxtFileItemProperties lPrefix = D2TxtFile.PREFIX
			.getRow(magic_prefix);
			String lPreName = lPrefix.get("Name");
			if (lPreName != null && !lPreName.equals("")) {
				iItemName = D2TblFile.getString(lPreName) + " " + iItemName;
				int lPreReq = getReq(lPrefix.get("levelreq"));
				if (lPreReq > iReqLvl) {
					iReqLvl = lPreReq;
				}
			}

			D2TxtFileItemProperties lSuffix = D2TxtFile.SUFFIX
			.getRow(magic_suffix);
			String lSufName = lSuffix.get("Name");
			if (lSufName != null && !lSufName.equals("")) {
				iItemName = iItemName + " " + D2TblFile.getString(lSufName);
				int lSufReq = getReq(lSuffix.get("levelreq"));
				if (lSufReq > iReqLvl) {
					iReqLvl = lSufReq;
				}
			}

			applyAutomodLvl();
			break;
		}
		case 5: // set item
		{
			iSet = true;
			set_id = (short) pFile.read(12);
			if (gfx_num == -1) {
				String s = (String) iItemType.get("setinvfile");
				if (s.compareTo("") != 0)
					image_file = s;
			}

			D2TxtFileItemProperties lSet = D2TxtFile.SETITEMS.getRow(set_id);
			iItemName = D2TblFile.getString(lSet.get("index"));
			iSetName = lSet.get("set");

			setSize = (D2TxtFile.SETITEMS.searchColumnsMultipleHits("set",
					iSetName)).size();

			int lSetReq = getReq(lSet.get("lvl req"));
			if (lSetReq != -1) {
				iReqLvl = lSetReq;
			}
			// else
			// {
			// System.err.println("Set Lvl: " + iItemName + " - " + lSetReq );
			// }
			applyAutomodLvl();
			break;
		}
		case 7: // unique item
		{
			iUnique = true;
			unique_id = (short) pFile.read(12);
			String s = iItemType.get("uniqueinvfile");
			if (s.compareTo("") != 0) {
				image_file = s;
			}

			D2TxtFileItemProperties lUnique = D2TxtFile.UNIQUES
			.getRow(unique_id);
			String lNewName = D2TblFile.getString(lUnique.get("index"));
			if (lNewName != null) {
				iItemName = lNewName;
			}

			int lUniqueReq = getReq(lUnique.get("lvl req"));
			if (lUniqueReq != -1) {
				iReqLvl = lUniqueReq;
			}
			// else
			// {
			// System.err.println("Unique Lvl: " + iItemName + " - " +
			// lUniqueReq );
			// }
			applyAutomodLvl();
			break;
		}
		case 6: // rare item
		{
			iRare = true;
			iItemName = "Rare " + iItemName;
		}
		case 8: // also a rare item, do the same (one's probably crafted)
		{
			if (quality == 8) {
				iCrafted = true;
				iItemName = "Crafted " + iItemName;
			}
		}
		applyAutomodLvl();
		rare_name_1 = (short) pFile.read(8);
		rare_name_2 = (short) pFile.read(8);
		D2TxtFileItemProperties lRareName1 = D2TxtFile.RAREPREFIX
		.getRow(rare_name_1 - 156);
		D2TxtFileItemProperties lRareName2 = D2TxtFile.RARESUFFIX
		.getRow(rare_name_2 - 1);
		iItemName = D2TblFile.getString(lRareName1.get("name")) + " "
		+ D2TblFile.getString(lRareName2.get("name"));

		rare_prefixes = new short[3];
		rare_suffixes = new short[3];
		short pre_count = 0;
		short suf_count = 0;
		for (int i = 0; i < 3; i++) {
			if (pFile.read(1) == 1) {
				rare_prefixes[pre_count] = (short) pFile.read(11);
				D2TxtFileItemProperties lPrefix = D2TxtFile.PREFIX
				.getRow(rare_prefixes[pre_count]);
				pre_count++;
				String lPreName = lPrefix.get("Name");
				if (lPreName != null && !lPreName.equals("")) {
					int lPreReq = getReq(lPrefix.get("levelreq"));
					if (lPreReq > iReqLvl) {
						iReqLvl = lPreReq;
					}
				}

			}
			if (pFile.read(1) == 1) {
				rare_suffixes[suf_count] = (short) pFile.read(11);
				D2TxtFileItemProperties lSuffix = D2TxtFile.SUFFIX
				.getRow(rare_suffixes[suf_count]);
				suf_count++;
				String lSufName = lSuffix.get("Name");
				if (lSufName != null && !lSufName.equals("")) {
					int lSufReq = getReq(lSuffix.get("levelreq"));
					if (lSufReq > iReqLvl) {
						iReqLvl = lSufReq;
					}
				}
			}
		}

//		System.out.println(pFile.read(1));
		if(isCrafted()){
			iReqLvl = 	iReqLvl + 10+(3* (suf_count + pre_count));
		}
		break;

		case 2: {
			readTypes(pFile);
			break;
		}
		}

		// rune word
		if (check_flag(27)) {
			pFile.skipBits(12);
			//runeword_id = (int) pFile.read(12);
			pFile.skipBits(4);
//			System.out.println(runeword_id);
//			System.out.println(D2TxtFile.RUNES.getRow(runeword_id).get("Rune Name"));
//			System.out.println(D2TblFile.getString(D2TxtFile.RUNES.getRow(runeword_id).get("Rune Name")));
		}
		iItemNameNoPersonalising = iItemName;
		// personalized
		if (check_flag(25)) {
			personalization = "";
			boolean lNotEnded = true;
			for (int i = 0; i < 15 && lNotEnded; i++) {
				char c = (char) pFile.read(7);
				if (c == 0) {
					lNotEnded = false;
				} else {
					personalization += c;
				}
			}
			if(lNotEnded == true){
				pFile.read(7);
			}
		}
	}

	private void applyAutomodLvl() {
		// modifies the level if the automod is higher
		if(automod_info == null){
			return;
		}
		if(Integer.parseInt(automod_info.get("levelreq")) > iReqLvl){
			iReqLvl = Integer.parseInt(automod_info.get("levelreq"));
		}

	}

	// MBR: unknown, but should be according to file format
	private void readTypes(D2BitReader pFile) {
		// charms ??
		if (isCharm()) {
			long lCharm1 = pFile.read(1);
			long lCharm2 = pFile.read(11);
			// System.err.println("Charm (?): " + lCharm1 );
			// System.err.println("Charm (?): " + lCharm2 );
		}

		// books / scrolls ??
		if ("tbk".equals(item_type) || "ibk".equals(item_type)) {
			long lTomb = pFile.read(5);
			// System.err.println("Tome ID: " + lTomb );
		}

		if ("tsc".equals(item_type) || "isc".equals(item_type)) {
			long lTomb = pFile.read(5);
			// System.err.println("Tome ID: " + lTomb );
		}

		// body ??
		if ("body".equals(item_type)) {
			long lMonster = pFile.read(10);
			// System.err.println("Monster ID: " + lMonster );
		}
	}

	private void readExtend2(D2BitReader pFile) throws Exception {
		if (isTypeArmor()) {
			// pFile.read(1);
			iDef = (pFile.read(11) - 10); // -10 ???
			iInitDef = iDef;
			iMaxDur = pFile.read(8);

			if (iMaxDur != 0) {
				iCurDur = pFile.read(9);
			}

		} else if (isTypeWeapon()) {
			if (iType.equals("tkni") || iType.equals("taxe")
					|| iType.equals("jave") || iType.equals("ajav")) {
				iThrow = true;
			}
			iMaxDur = pFile.read(8);

			if (iMaxDur != 0) {
				iCurDur = pFile.read(9);
			}

			if ((D2TxtFile.WEAPONS.searchColumns("code", item_type)).get(
			"1or2handed").equals("")
			&& !iThrow) {

				if ((D2TxtFile.WEAPONS.searchColumns("code", item_type)).get(
				"2handed").equals("1")) {
					iWhichHand = 2;
					iinitMinDmg = iMinDmg = Long.parseLong((D2TxtFile.WEAPONS
							.searchColumns("code", item_type))
							.get("2handmindam"));
					iinitMaxDmg = iMaxDmg = Long.parseLong((D2TxtFile.WEAPONS
							.searchColumns("code", item_type))
							.get("2handmaxdam"));
				} else {
					iWhichHand = 1;
					iinitMinDmg = iMinDmg = Long.parseLong((D2TxtFile.WEAPONS
							.searchColumns("code", item_type)).get("mindam"));
					iinitMaxDmg = iMaxDmg = Long.parseLong((D2TxtFile.WEAPONS
							.searchColumns("code", item_type)).get("maxdam"));
				}

			} else {
				iWhichHand = 0;
				if (iThrow) {
					iinit2MinDmg = i2MinDmg = Long
					.parseLong((D2TxtFile.WEAPONS.searchColumns("code",
							item_type)).get("minmisdam"));
					iinit2MaxDmg = i2MaxDmg = Long
					.parseLong((D2TxtFile.WEAPONS.searchColumns("code",
							item_type)).get("maxmisdam"));
				} else {
					iinit2MinDmg = i2MinDmg = Long.parseLong((D2TxtFile.WEAPONS
							.searchColumns("code", item_type))
							.get("2handmindam"));
					iinit2MaxDmg = i2MaxDmg = Long.parseLong((D2TxtFile.WEAPONS
							.searchColumns("code", item_type))
							.get("2handmaxdam"));
				}
				iinitMinDmg = iMinDmg = Long.parseLong((D2TxtFile.WEAPONS
						.searchColumns("code", item_type)).get("mindam"));
				iinitMaxDmg = iMaxDmg = Long.parseLong((D2TxtFile.WEAPONS
						.searchColumns("code", item_type)).get("maxdam"));
			}

			if ("1".equals(iItemType.get("stackable"))) {
				// System.err.println("Test: " + iItemType.get("stackable") + "
				// - " + iItemType.get("minstack")
				// + " - " + iItemType.get("maxstack") + " - " +
				// iItemType.get("spawnstack") );
				iStackable = true;
				iCurDur = pFile.read(9);
			}
		} else if (isTypeMisc()) {
			if ("1".equals(iItemType.get("stackable"))) {
				iStackable = true;
				iCurDur = pFile.read(9);
			}

		}

		if (iSocketed) {
			iGemProps = new ArrayList();
			iSocketNrTotal = pFile.read(4);
			// System.err.println("Nr Sockets: " + lNrSockets );
		}

		lSet1 = 0;
		lSet2 = 0;
		lSet3 = 0;
		lSet4 = 0;
		lSet5 = 0;

		if (quality == 5) {
			lSet1 = pFile.read(1);
			lSet2 = pFile.read(1);
			lSet3 = pFile.read(1);
			lSet4 = pFile.read(1);
			lSet5 = pFile.read(1);
		}
//		System.out.println(iItemName + ", " + iFP + "," + pFile.get_byte_pos());
//		readProperties(pFile, iProperties);
		readPropertiesNew(pFile);


		if (quality == 5) {
			iSetProps = new ArrayList();
			if (lSet1 == 1) {
				iSet1 = new ArrayList();
				readProperties(pFile, iSet1);
			}
			if (lSet2 == 1) {
				iSet2 = new ArrayList();
				readProperties(pFile, iSet2);
			}
			if (lSet3 == 1) {
				iSet3 = new ArrayList();
				readProperties(pFile, iSet3);
			}
			if (lSet4 == 1) {
				iSet4 = new ArrayList();
				readProperties(pFile, iSet4);
			}
			if (lSet5 == 1) {
				iSet5 = new ArrayList();
				readProperties(pFile, iSet5);
			}
		}

		if (iRuneWord) {
			iRuneWordProps = new ArrayList();
			readProperties(pFile, iRuneWordProps);
			// long lProp7 = pFile.read(9);

		}
//		cleanUpProperties();

	}


//	private void cleanUpProperties() {
//
//		
//		//THROW DAMAGE AND 2H MIN/MAX DAMAGE
//		
////		for (int x = 0; x < iProperties.size(); x = x + 1) {
////			if (((D2ItemProperty) iProperties.get(x)).getiProp() == 160
////					|| ((D2ItemProperty) iProperties.get(x)).getiProp() == 159) {
////				iProperties.remove(x);
////				x = x - 1;
////			}
////			if (((D2ItemProperty) iProperties.get(x)).getiProp() == 23
////					|| ((D2ItemProperty) iProperties.get(x)).getiProp() == 24) {
////				cleanUp2HMax();
////			}
////		}
//
////
////		if(isRuneWord()){
////			for (int x = 0; x < iRuneWordProps.size(); x = x + 1) {
////				if (((D2ItemProperty) iRuneWordProps.get(x)).getiProp() == 160
////						|| ((D2ItemProperty) iRuneWordProps.get(x)).getiProp() == 159) {
////					iRuneWordProps.remove(x);
////					x = x - 1;
////				}
////				if (((D2ItemProperty) iRuneWordProps.get(x)).getiProp() == 23
////						|| ((D2ItemProperty) iRuneWordProps.get(x)).getiProp() == 24) {
////					cleanUp2HMax();
////				}
////			}
////
////		}
////
//
//	}

//	private void cleanUp2HMax() {
//
//		ArrayList cleanArr = new ArrayList();
//
//		for (int x = 0; x < iProperties.size(); x = x + 1) {
//			cleanArr.add(new Integer(((D2ItemProperty) iProperties.get(x))
//					.getiProp()));
//
//		}
//
//		if (cleanArr.contains(new Integer(24))
//				&& cleanArr.contains(new Integer(22))
//				&& cleanArr.contains(new Integer(160))) {
//			iProperties.remove(cleanArr.indexOf(new Integer(160)));
//			cleanArr.remove(new Integer(160));
//			iProperties.remove(cleanArr.indexOf(new Integer(24)));
//			cleanArr.remove(new Integer(24));
//		}
//		if (cleanArr.contains(new Integer(23))
//				&& cleanArr.contains(new Integer(21))
//				&& cleanArr.contains(new Integer(159))) {
//			iProperties.remove(cleanArr.indexOf(new Integer(159)));
//			cleanArr.remove(new Integer(159));
//			iProperties.remove(cleanArr.indexOf(new Integer(23)));
//			cleanArr.remove(new Integer(23));
//		}
//
//		if (cleanArr.contains(new Integer(24))
//				&& cleanArr.contains(new Integer(22))) {
//			iProperties.remove(cleanArr.indexOf(new Integer(24)));
//			cleanArr.remove(new Integer(24));
//		}
//		if (cleanArr.contains(new Integer(23))
//				&& cleanArr.contains(new Integer(21))) {
//			iProperties.remove(cleanArr.indexOf(new Integer(23)));
//			cleanArr.remove(new Integer(23));
//		}
//
//		if (cleanArr.contains(new Integer(23))) {
//			D2ItemProperty lProperty = new D2ItemProperty(21, iCharLvl,
//					iItemName);
//			lProperty.set(21, ((D2ItemProperty) iProperties.get(cleanArr
//					.indexOf(new Integer(23)))).getItemStatCost(),
//					((D2ItemProperty) iProperties.get(cleanArr
//							.indexOf(new Integer(23)))).getBitSet(),
//							((D2ItemProperty) iProperties.get(cleanArr
//									.indexOf(new Integer(23)))).getRealValue());
//			iProperties.remove(cleanArr.indexOf(new Integer(23)));
//			iProperties.add(cleanArr.indexOf(new Integer(23)), lProperty);
//		}
//
//		if (cleanArr.contains(new Integer(24))) {
//			D2ItemProperty lProperty = new D2ItemProperty(22, iCharLvl,
//					iItemName);
//			lProperty.set(22, ((D2ItemProperty) iProperties.get(cleanArr
//					.indexOf(new Integer(24)))).getItemStatCost(),
//					((D2ItemProperty) iProperties.get(cleanArr
//							.indexOf(new Integer(24)))).getBitSet(),
//							((D2ItemProperty) iProperties.get(cleanArr
//									.indexOf(new Integer(24)))).getRealValue());
//			iProperties.remove(cleanArr.indexOf(new Integer(24)));
//			iProperties.add(cleanArr.indexOf(new Integer(24)), lProperty);
//		}
//
//		if(isRuneWord()){
//
//			cleanArr = new ArrayList();
//
//			for (int x = 0; x < iRuneWordProps.size(); x = x + 1) {
//				cleanArr.add(new Integer(((D2ItemProperty) iRuneWordProps.get(x))
//						.getiProp()));
//
//			}
//
//			if (cleanArr.contains(new Integer(24))
//					&& cleanArr.contains(new Integer(22))
//					&& cleanArr.contains(new Integer(160))) {
//				iRuneWordProps.remove(cleanArr.indexOf(new Integer(160)));
//				cleanArr.remove(new Integer(160));
//				iRuneWordProps.remove(cleanArr.indexOf(new Integer(24)));
//				cleanArr.remove(new Integer(24));
//			}
//			if (cleanArr.contains(new Integer(23))
//					&& cleanArr.contains(new Integer(21))
//					&& cleanArr.contains(new Integer(159))) {
//				iRuneWordProps.remove(cleanArr.indexOf(new Integer(159)));
//				cleanArr.remove(new Integer(159));
//				iRuneWordProps.remove(cleanArr.indexOf(new Integer(23)));
//				cleanArr.remove(new Integer(23));
//			}
//
//			if (cleanArr.contains(new Integer(24))
//					&& cleanArr.contains(new Integer(22))) {
//				iRuneWordProps.remove(cleanArr.indexOf(new Integer(24)));
//				cleanArr.remove(new Integer(24));
//			}
//			if (cleanArr.contains(new Integer(23))
//					&& cleanArr.contains(new Integer(21))) {
//				iRuneWordProps.remove(cleanArr.indexOf(new Integer(23)));
//				cleanArr.remove(new Integer(23));
//			}
//
//			if (cleanArr.contains(new Integer(23))) {
//				D2ItemProperty lProperty = new D2ItemProperty(21, iCharLvl,
//						iItemName);
//				lProperty.set(21, ((D2ItemProperty) iRuneWordProps.get(cleanArr
//						.indexOf(new Integer(23)))).getItemStatCost(),
//						((D2ItemProperty) iRuneWordProps.get(cleanArr
//								.indexOf(new Integer(23)))).getBitSet(),
//								((D2ItemProperty) iRuneWordProps.get(cleanArr
//										.indexOf(new Integer(23)))).getRealValue());
//				iRuneWordProps.remove(cleanArr.indexOf(new Integer(23)));
//				iRuneWordProps.add(cleanArr.indexOf(new Integer(23)), lProperty);
//			}
//
//			if (cleanArr.contains(new Integer(24))) {
//				D2ItemProperty lProperty = new D2ItemProperty(22, iCharLvl,
//						iItemName);
//				lProperty.set(22, ((D2ItemProperty) iRuneWordProps.get(cleanArr
//						.indexOf(new Integer(24)))).getItemStatCost(),
//						((D2ItemProperty) iRuneWordProps.get(cleanArr
//								.indexOf(new Integer(24)))).getBitSet(),
//								((D2ItemProperty) iRuneWordProps.get(cleanArr
//										.indexOf(new Integer(24)))).getRealValue());
//				iRuneWordProps.remove(cleanArr.indexOf(new Integer(24)));
//				iRuneWordProps.add(cleanArr.indexOf(new Integer(24)), lProperty);
//			}
//
//		}
//
//	}

	private String getExStr() {
		return " (" + iItemName + ", " + iFP + ")";
	}

	private void readPropertiesPots(D2BitReader pfile) {

		String[] statsToRead = { "stat1", "stat2" };

		for (int x = 0; x < statsToRead.length; x = x + 1) {
			String txtCheck = (D2TxtFile.MISC.searchColumns("code", item_type))
			.get(statsToRead[x]);

			if (!txtCheck.equals("")) {

				int lProp = Integer.parseInt((D2TxtFile.ITEM_STAT_COST
						.searchColumns("Stat", txtCheck)).get("ID"));

				D2ItemProperty lProperty = new D2ItemProperty(lProp, iCharLvl,
						iItemName);

//				iProperties.add(lProperty);

				D2TxtFileItemProperties lItemStatCost = D2TxtFile.ITEM_STAT_COST
				.getRow(lProperty.getPropNrs()[0]);

				lProperty.set(lProp, lItemStatCost, 0, Long
						.parseLong(((D2TxtFile.MISC.searchColumns("code",
								item_type)).get(statsToRead[x].replaceFirst(
										"stat", "calc")))));

			}

		}

	}

	private void readPropertiesRunes(D2BitReader pfile, ArrayList pProperties) {

		ArrayList wepProps = new ArrayList();
		ArrayList armProps = new ArrayList();
		ArrayList shiProps = new ArrayList();

		String[][] interestingSubProp = {
				{ "weaponMod1Code", "weaponMod2Code", "weaponMod3Code" },
				{ "helmMod1Code", "helmMod2Code", "helmMod3Code" },
				{ "shieldMod1Code", "shieldMod2Code", "shieldMod3Code" } };
		// String[] interestingProp = {"weaponMod1Code", "helmMod1Code",
		// "shieldMod1Code"};

		for (int x = 0; x < interestingSubProp.length; x = x + 1) {

			for (int y = 0; y < interestingSubProp[x].length; y = y + 1) {

				String txtCheck = (D2TxtFile.GEMS.searchColumns("code",
						item_type)).get(interestingSubProp[x][y]);

				if (!txtCheck.equals("")) {
					int lProp = Integer.parseInt((D2TxtFile.ITEM_STAT_COST
							.searchColumns("Stat", ((D2TxtFile.PROPS
									.searchColumns("code", (D2TxtFile.GEMS
											.searchColumns("code", item_type))
											.get(interestingSubProp[x][y])))
											.get("stat1")))).get("ID"));
					D2ItemProperty lProperty = new D2ItemProperty(lProp,
							iCharLvl, iItemName);
					D2TxtFileItemProperties lItemStatCost = D2TxtFile.ITEM_STAT_COST
					.getRow(lProperty.getPropNrs()[0]);

					String runeParam = (D2TxtFile.GEMS.searchColumns("code",
							item_type)).get(interestingSubProp[x][y]
							                                      .replaceFirst("Code", "Param"));

					String runeMin = (D2TxtFile.GEMS.searchColumns("code",
							item_type)).get(interestingSubProp[x][y]
							                                      .replaceFirst("Code", "Min"));

					String runeMax = (D2TxtFile.GEMS.searchColumns("code",
							item_type)).get(interestingSubProp[x][y]
							                                      .replaceFirst("Code", "Max"));

					lProperty.set(lProp, lItemStatCost, 0, Long
							.parseLong(runeMin));

					if (txtCheck.equals("res-all")) {
						lProperty = new D2ItemProperty(1337, iCharLvl,
								iItemName);
						lProperty.set(1337, lItemStatCost, 0, Long
								.parseLong(runeMax));
					} else if (!runeMax.equals("")) {
						lProperty.set(lProp, lItemStatCost, 0, Long
								.parseLong(runeMax));
					}

					if (!runeParam.equals("")) {

						// lProperty.set(lProp, lItemStatCost, 0,
						// Long.parseLong("1337"));

						lProperty.set(lProp, lItemStatCost, 0, Long
								.parseLong(runeParam));
					}

					if (interestingSubProp[x][0].split("Mod")[0]
					                                          .equals("weapon")) {
						wepProps.add(lProperty);
					} else if (interestingSubProp[x][0].split("Mod")[0]
					                                                 .equals("helm")) {
						armProps.add(lProperty);
					} else {
						shiProps.add(lProperty);
					}
				}
			}

		}
		pProperties.add(wepProps);
		pProperties.add(armProps);
		pProperties.add(shiProps);

	}

	private void readPropertiesGems(D2BitReader pFile, ArrayList pProperties) {

		ArrayList wepProps = new ArrayList();
		ArrayList armProps = new ArrayList();
		ArrayList shiProps = new ArrayList();

		String[][] interestingSubProp = {
				{ "weaponMod1Code", "weaponMod2Code", "weaponMod3Code" },
				{ "helmMod1Code", "helmMod2Code", "helmMod3Code" },
				{ "shieldMod1Code", "shieldMod2Code", "shieldMod3Code" } };
		// String[] interestingProp = {"weaponMod1Code", "helmMod1Code",
		// "shieldMod1Code"};

		for (int x = 0; x < interestingSubProp.length; x = x + 1) {

			String[] propStats = {
					((D2TxtFile.PROPS.searchColumns("code", (D2TxtFile.GEMS
							.searchColumns("code", item_type))
							.get(interestingSubProp[x][0]))).get("stat1")),
							((D2TxtFile.PROPS.searchColumns("code", (D2TxtFile.GEMS
									.searchColumns("code", item_type))
									.get(interestingSubProp[x][0]))).get("stat2")),
									((D2TxtFile.PROPS.searchColumns("code", (D2TxtFile.GEMS
											.searchColumns("code", item_type))
											.get(interestingSubProp[x][0]))).get("stat3")),
											((D2TxtFile.PROPS.searchColumns("code", (D2TxtFile.GEMS
													.searchColumns("code", item_type))
													.get(interestingSubProp[x][0]))).get("stat4")) };

			for (int z = 0; z < propStats.length; z = z + 1) {
				if (propStats[z].equals("")) {
					break;
				}
				int lProp = Integer.parseInt((D2TxtFile.ITEM_STAT_COST
						.searchColumns("Stat", propStats[z])).get("ID"));
				D2ItemProperty lProperty = new D2ItemProperty(lProp, iCharLvl,
						iItemName);

				D2TxtFileItemProperties lItemStatCost = D2TxtFile.ITEM_STAT_COST
				.getRow(lProperty.getPropNrs()[0]);
				for (int y = 0; y < interestingSubProp[x].length; y = y + 1) {

					String txtCheck = (D2TxtFile.GEMS.searchColumns("code",
							item_type)).get(interestingSubProp[x][y]);

					if (!txtCheck.equals("")) {

						if (txtCheck.equals("res-all")) {
							lProperty = new D2ItemProperty(1337, iCharLvl,
									iItemName);
							lProperty
							.set(
									1337,
									lItemStatCost,
									0,
									Long
									.parseLong((D2TxtFile.GEMS
											.searchColumns(
													"code",
													item_type))
													.get(interestingSubProp[x][0]
													                           .replaceFirst(
													                        		   "Code",
													                           "Min"))));
							z = propStats.length;
						} else {
							lProperty
							.set(
									lProp,
									lItemStatCost,
									0,
									Long
									.parseLong((D2TxtFile.GEMS
											.searchColumns(
													"code",
													item_type))
													.get(interestingSubProp[x][y]
													                           .replaceFirst(
													                        		   "Code",
													                           "Min"))));
						}

					}

				}

				if (lProp == 62) {
					D2ItemProperty lProperty2 = new D2ItemProperty(60,
							iCharLvl, iItemName);
					D2TxtFileItemProperties lItemStatCost2 = D2TxtFile.ITEM_STAT_COST
					.getRow(lProperty.getPropNrs()[0] - 2);
					lProperty2.set(60, lItemStatCost2, 0, Long
							.parseLong((D2TxtFile.GEMS.searchColumns("code",
									item_type)).get("weaponMod2Min")));
					wepProps.add(lProperty2);
				}

				if (interestingSubProp[x][0].split("Mod")[0].equals("weapon")) {
					wepProps.add(lProperty);
				} else if (interestingSubProp[x][0].split("Mod")[0]
				                                                 .equals("helm")) {
					armProps.add(lProperty);
				} else {
					shiProps.add(lProperty);
				}
			}
		}

		pProperties.add(wepProps);
		pProperties.add(armProps);
		pProperties.add(shiProps);
	}


	private void readProperties(D2BitReader pFile, ArrayList pProperties) {

		int lProp = (int) pFile.read(9);

		while (lProp != 511) {

			D2ItemProperty lProperty = new D2ItemProperty(lProp, iCharLvl,
					iItemName);
			pProperties.add(lProperty);
			int lRead[] = lProperty.getPropNrs();

			for (int i = 0; i < lRead.length; i++) {
				D2TxtFileItemProperties lItemStatCost = D2TxtFile.ITEM_STAT_COST
				.getRow(lRead[i]);

				String lItemStatCostList[];


				if (lProp == 201 || lProp == 197 || lProp == 199
						|| lProp == 195 || lProp == 198 || lProp == 196) {
					// extra splitup


					lItemStatCostList = new String[] { "6", "10",
							lItemStatCost.get("Save Bits") };
				} else if (lProp == 204) {
					lItemStatCostList = new String[] { "6", "10", "8", "8" };
				} else {
					lItemStatCostList = new String[] {
							lItemStatCost.get("Save Param Bits"),
							lItemStatCost.get("Save Bits") };
				}



				for (int k = 0; k < lItemStatCostList.length; k++) {
					if (!("".equals(lItemStatCostList[k]))) {

						int lBits = Integer.parseInt(lItemStatCostList[k]);
						long lValue = pFile.read(lBits);
//						System.err.println("Property " +
//						lItemStatCost.get("Stat") + ": " + lValue + " - " +
//						lItemStatCost.get("Save Add") + " - " + lBits);
						String lSaveAdd = lItemStatCost.get("Save Add");
						if (lSaveAdd != null && !"".equals(lSaveAdd)) {
							try {
								long lLoadSubtract = Long.parseLong(lSaveAdd);
								lValue -= lLoadSubtract;
							} catch (Exception pEx) {
								D2FileManager.displayErrorDialog(pEx);
							}
						}
						lProperty.set(lRead[i], lItemStatCost, k, lValue);

					}
				}
			}
			lProp = (int) pFile.read(9);
		}



	}

	private void readPropertiesNew(D2BitReader pFile) {


		int rootProp = (int) pFile.read(9);
		
		while(rootProp != 511){
			
			readProp(pFile, rootProp);

			if (rootProp == 17)
			{
				readProp(pFile,18);
			}
			else if (rootProp == 48)
			{
				readProp(pFile,49);
			}
			else if (rootProp == 50)
			{
				readProp(pFile,51);
			}
			else if (rootProp == 52)
			{
				readProp(pFile,53);
			}
			else if (rootProp == 54)
			{
				readProp(pFile,55);
				readProp(pFile,56);
			}
			else if (rootProp == 57)
			{
				readProp(pFile,58);
				readProp(pFile,59);
			}

			rootProp = (int)pFile.read(9);
			
		}
	}

	private void readProp(D2BitReader pFile, int rootProp) {

			D2TxtFileItemProperties pRow = D2TxtFile.ITEM_STAT_COST.getRow(rootProp);
//			System.out.println(rootProp + " , " + getName());
			int readLength = Integer.parseInt(pRow.get("Save Bits"));
			int saveAdd = 0;
			if(!pRow.get("Save Add").equals("")){
				saveAdd = Integer.parseInt(pRow.get("Save Add"));
			}
			if (rootProp == 201 || rootProp == 197 || rootProp == 199
					|| rootProp == 195 || rootProp == 198 || rootProp == 196) {
				iProps.add(new D2Prop(rootProp, new int[] {(int)pFile.read(6)-saveAdd,(int)pFile.read(10)-saveAdd,(int)pFile.read(readLength) - saveAdd}));
			} else if (rootProp == 204) {
				iProps.add(new D2Prop(rootProp, new int[] {(int)pFile.read(6)-saveAdd,(int)pFile.read(10)-saveAdd,(int)pFile.read(8)-saveAdd,(int)pFile.read(8)-saveAdd}));
			} else if(!pRow.get("Save Param Bits").equals("")){
				iProps.add(new D2Prop(rootProp,new int[] {(int)pFile.read(Integer.parseInt(pRow.get("Save Param Bits"))) - saveAdd,(int)pFile.read(readLength) - saveAdd}));
			} else {
				iProps.add(new D2Prop(rootProp,new int[] {(int)pFile.read(readLength) - saveAdd}));
			}

	}

	private void applyEthDmg() {
		iinitMinDmg = iMinDmg = (long) Math
		.floor((((double) iMinDmg / (double) 100) * (double) 50)
				+ iMinDmg);
		iinitMaxDmg = iMaxDmg = (long) Math
		.floor((((double) iMaxDmg / (double) 100) * (double) 50)
				+ iMaxDmg);

		if (iWhichHand == 0) {
			iinit2MinDmg = i2MinDmg = (long) Math
			.floor((((double) i2MinDmg / (double) 100) * (double) 50)
					+ i2MinDmg);
			iinit2MaxDmg = i2MaxDmg = (long) Math
			.floor((((double) i2MaxDmg / (double) 100) * (double) 50)
					+ i2MaxDmg);
		}
	}

	private void modifyLvl(ArrayList skillArr) {
		for (int x = 0; x < skillArr.size(); x = x + 1) {

			D2TxtFileItemProperties lSkill = D2TxtFile.SKILL_DESC
			.getRow((int) ((D2ItemProperty) skillArr.get(x))
					.getRealValue());

			if (iReqLvl < Integer.parseInt(D2TxtFile.SKILLS.searchColumns(
					"skilldesc", lSkill.get("skilldesc")).get("reqlevel"))) {

				iReqLvl = (Integer.parseInt(D2TxtFile.SKILLS.searchColumns(
						"skilldesc", lSkill.get("skilldesc")).get("reqlevel")));
			}
		}
	}

//	private void applyEDef() {
//
//
//
//
//
//		iDef = 0;
//		int ENDef = 0;
//		int Def = 0;
//		int Dur = 0;
//		int PlusDur = 0;
//
//		if (isSet()) {
//
//			for (int x = 0; x < iSetProps.size(); x = x + 1) {
//				if (((D2ItemProperty) iSetProps.get(x)).getiProp() == 16) {
//					ENDef = ENDef
//					+ ((D2ItemProperty) iSetProps.get(x))
//					.getRealValue();
//				}
//				if (((D2ItemProperty) iSetProps.get(x)).getiProp() == 31) {
//					Def = Def
//					+ ((D2ItemProperty) iSetProps.get(x))
//					.getRealValue();
//				}
//				if (((D2ItemProperty) iSetProps.get(x)).getiProp() == 75) {
//					Dur = Dur
//					+ ((D2ItemProperty) iSetProps.get(x))
//					.getRealValue();
//				}
//				if (((D2ItemProperty) iSetProps.get(x)).getiProp() == 73) {
//					PlusDur = PlusDur
//					+ ((D2ItemProperty) iSetProps.get(x))
//					.getRealValue();
//				}
//				if (((D2ItemProperty) iSetProps.get(x)).getiProp() == 214) {
//					Def = Def
//					+ (int) Math.floor((((D2ItemProperty) iSetProps
//							.get(x)).getRealValue() * 0.125)
//							* iCharLvl);
//				}
//			}
//
//		}
//
//		if (isSocketed()) {
//
//			if (isRuneWord()) {
//				for (int x = 0; x < iRuneWordProps.size(); x = x + 1) {
//					if (((D2ItemProperty) iRuneWordProps.get(x)).getiProp() == 16) {
//
//						ENDef = ENDef
//						+ ((D2ItemProperty) iRuneWordProps.get(x))
//						.getRealValue();
//					}
//					if (((D2ItemProperty) iRuneWordProps.get(x)).getiProp() == 31) {
//						Def = Def
//						+ ((D2ItemProperty) iRuneWordProps.get(x))
//						.getRealValue();
//					}
//					if (((D2ItemProperty) iRuneWordProps.get(x)).getiProp() == 75) {
//						Dur = Dur
//						+ ((D2ItemProperty) iRuneWordProps.get(x))
//						.getRealValue();
//					}
//					if (((D2ItemProperty) iRuneWordProps.get(x)).getiProp() == 73) {
//						PlusDur = PlusDur
//						+ ((D2ItemProperty) iRuneWordProps.get(x))
//						.getRealValue();
//					}
//					if (((D2ItemProperty) iRuneWordProps.get(x)).getiProp() == 214) {
//						Def = Def
//						+ (int) Math
//						.floor((((D2ItemProperty) iRuneWordProps
//								.get(x)).getRealValue() * 0.125)
//								* iCharLvl);
//					}
//				}
//			}
//
//			for (int x = 0; x < iGemProps.size(); x = x + 1) {
//				if (((D2ItemProperty) iGemProps.get(x)).getiProp() == 16) {
//					ENDef = ENDef
//					+ ((D2ItemProperty) iGemProps.get(x))
//					.getRealValue();
//				}
//				if (((D2ItemProperty) iGemProps.get(x)).getiProp() == 31) {
//					Def = Def
//					+ ((D2ItemProperty) iGemProps.get(x))
//					.getRealValue();
//				}
//				if (((D2ItemProperty) iGemProps.get(x)).getiProp() == 75) {
//					Dur = Dur
//					+ ((D2ItemProperty) iGemProps.get(x))
//					.getRealValue();
//				}
//				if (((D2ItemProperty) iGemProps.get(x)).getiProp() == 73) {
//					PlusDur = PlusDur
//					+ ((D2ItemProperty) iGemProps.get(x))
//					.getRealValue();
//				}
//				if (((D2ItemProperty) iGemProps.get(x)).getiProp() == 214) {
//					Def = Def
//					+ (int) Math.floor((((D2ItemProperty) iGemProps
//							.get(x)).getRealValue() * 0.125)
//							* iCharLvl);
//				}
//			}
//
//		}
//		for (int x = 0; x < iProperties.size(); x = x + 1) {
//			if (((D2ItemProperty) iProperties.get(x)).getiProp() == 16) {
//				ENDef = ENDef
//				+ ((D2ItemProperty) iProperties.get(x)).getRealValue();
//			}
//			if (((D2ItemProperty) iProperties.get(x)).getiProp() == 31) {
//				Def = Def
//				+ ((D2ItemProperty) iProperties.get(x)).getRealValue();
//			}
//			if (((D2ItemProperty) iProperties.get(x)).getiProp() == 75) {
//				Dur = Dur
//				+ ((D2ItemProperty) iProperties.get(x)).getRealValue();
//			}
//			if (((D2ItemProperty) iProperties.get(x)).getiProp() == 73) {
//				PlusDur = PlusDur
//				+ ((D2ItemProperty) iProperties.get(x)).getRealValue();
//			}
//			if (((D2ItemProperty) iProperties.get(x)).getiProp() == 214) {
//				Def = Def
//				+ (int) Math.floor((((D2ItemProperty) iProperties
//						.get(x)).getRealValue() * 0.125)
//						* iCharLvl);
//			}
//		}
//		iDef = (long) Math.floor((((double) iInitDef / (double) 100) * ENDef)
//				+ (iInitDef + Def));
//		iMaxDur = (long) Math.floor((((double) iMaxDur / (double) 100) * Dur)
//				+ (iMaxDur + PlusDur));
//
//	}

//	private void applyBlock() {	
//
//		int block = 0;
//
//		if (isSet()) {
//
//			for (int x = 0; x < iSetProps.size(); x = x + 1) {
//				if (((D2ItemProperty) iSetProps.get(x)).getiProp() == 20) {
//					block = block + ((D2ItemProperty) iSetProps.get(x)).getRealValue();	
//				}
//			}
//
//		}
//
//		if (isSocketed()) {
//
//			if (isRuneWord()) {
//				for (int x = 0; x < iRuneWordProps.size(); x = x + 1) {
//					if (((D2ItemProperty) iRuneWordProps.get(x)).getiProp() == 20) {
//						block = block + ((D2ItemProperty) iRuneWordProps.get(x)).getRealValue();	
//					}
//				}
//			}
//
//			for (int x = 0; x < iGemProps.size(); x = x + 1) {
//				if (((D2ItemProperty) iGemProps.get(x)).getiProp() == 20) {
//					block = block + ((D2ItemProperty) iGemProps.get(x)).getRealValue();	
//				}
//			}
//
//		}
//		for (int x = 0; x < iProperties.size(); x = x + 1) {
//			if (((D2ItemProperty) iProperties.get(x)).getiProp() == 20) {
//				block = block + ((D2ItemProperty) iProperties.get(x)).getRealValue();	
//			}
//		}
//
//		iBlock = cBlock + block;
//
//	}

//	private void applyEDmg() {
//
//		int ENDam = 0;
//		int ENMaxDam = 0;
//		int MinDam = 0;
//		int MaxDam = 0;
//
//		if (isSocketed()) {
//
//			if (isRuneWord()) {
//				for (int x = 0; x < iRuneWordProps.size(); x = x + 1) {
//					if (((D2ItemProperty) iRuneWordProps.get(x)).getiProp() == 17) {
//						ENDam = ENDam
//						+ ((D2ItemProperty) iRuneWordProps.get(x))
//						.getRealValue();
//					}
//					if (((D2ItemProperty) iRuneWordProps.get(x)).getiProp() == 219) {
//						ENMaxDam = ENMaxDam
//						+ (int) Math
//						.floor((((D2ItemProperty) iRuneWordProps
//								.get(x)).getRealValue() * 0.125)
//								* iCharLvl);
//					}
//					if (((D2ItemProperty) iRuneWordProps.get(x)).getiProp() == 218) {
//						MaxDam = MaxDam
//						+ (int) Math
//						.floor((((D2ItemProperty) iRuneWordProps
//								.get(x)).getRealValue() * 0.125)
//								* iCharLvl);
//					}
//					if (((D2ItemProperty) iRuneWordProps.get(x)).getiProp() == 21) {
//						MinDam = MinDam
//						+ ((D2ItemProperty) iRuneWordProps.get(x))
//						.getRealValue();
//					}
//					if (((D2ItemProperty) iRuneWordProps.get(x)).getiProp() == 22) {
//						MaxDam = MaxDam
//						+ ((D2ItemProperty) iRuneWordProps.get(x))
//						.getRealValue();
//					}
//				}
//			}
//
//			for (int x = 0; x < iGemProps.size(); x = x + 1) {
//				if (((D2ItemProperty) iGemProps.get(x)).getiProp() == 17) {
//					ENDam = ENDam
//					+ ((D2ItemProperty) iGemProps.get(x))
//					.getRealValue();
//				}
//				if (((D2ItemProperty) iGemProps.get(x)).getiProp() == 21) {
//					MinDam = MinDam
//					+ ((D2ItemProperty) iGemProps.get(x))
//					.getRealValue();
//				}
//				if (((D2ItemProperty) iGemProps.get(x)).getiProp() == 22) {
//					MaxDam = MaxDam
//					+ ((D2ItemProperty) iGemProps.get(x))
//					.getRealValue();
//				}
//				if (((D2ItemProperty) iGemProps.get(x)).getiProp() == 219) {
//					ENMaxDam = ENMaxDam
//					+ (int) Math.floor((((D2ItemProperty) iGemProps
//							.get(x)).getRealValue() * 0.125)
//							* iCharLvl);
//				}
//				if (((D2ItemProperty) iGemProps.get(x)).getiProp() == 218) {
//					MaxDam = MaxDam
//					+ (int) Math.floor((((D2ItemProperty) iGemProps
//							.get(x)).getRealValue() * 0.125)
//							* iCharLvl);
//				}
//			}
//
//		}
//		for (int x = 0; x < iProperties.size(); x = x + 1) {
//			if (((D2ItemProperty) iProperties.get(x)).getiProp() == 17) {
//				ENDam = ENDam
//				+ ((D2ItemProperty) iProperties.get(x)).getRealValue();
//			}
//			if (((D2ItemProperty) iProperties.get(x)).getiProp() == 21) {
//				MinDam = MinDam
//				+ ((D2ItemProperty) iProperties.get(x)).getRealValue();
//			}
//			if (((D2ItemProperty) iProperties.get(x)).getiProp() == 22) {
//				MaxDam = MaxDam
//				+ ((D2ItemProperty) iProperties.get(x)).getRealValue();
//			}
//			if (((D2ItemProperty) iProperties.get(x)).getiProp() == 219) {
//				ENMaxDam = ENMaxDam
//				+ (int) Math.floor((((D2ItemProperty) iProperties
//						.get(x)).getRealValue() * 0.125)
//						* iCharLvl);
//			}
//			if (((D2ItemProperty) iProperties.get(x)).getiProp() == 218) {
//				MaxDam = MaxDam
//				+ (int) Math.floor((((D2ItemProperty) iProperties
//						.get(x)).getRealValue() * 0.125)
//						* iCharLvl);
//			}
//		}
//		iMinDmg = (long) Math.floor((((double) iMinDmg / (double) 100) * ENDam)
//				+ (iMinDmg + MinDam));
//		iMaxDmg = (long) Math
//		.floor((((double) iMaxDmg / (double) 100) * (ENDam + ENMaxDam))
//				+ (iMaxDmg + MaxDam));
//
//		if (iWhichHand == 0) {
//			i2MinDmg = (long) Math
//			.floor((((double) i2MinDmg / (double) 100) * ENDam)
//					+ (i2MinDmg + MinDam));
//			i2MaxDmg = (long) Math
//			.floor((((double) i2MaxDmg / (double) 100) * (ENDam + ENMaxDam))
//					+ (i2MaxDmg + MaxDam));
//		}
//
//	}

	// public ArrayList getAllProps(){
	// if(null != propsList){
	// int[] propsListOut = new int[propsList.size()];
	//		
	// for(int x = 0;x<propsList.size();x=x+1){
	// propsListOut[x] = ((D2ItemProperty)propsList.get(x)).getiProp();
	// System.out.println(propsListOut[x]);
	// }
	//		
	// return propsListOut;
	// }else{
	// return new int[0];
	// }
	// }

//	private void combineProps() {
//
//
//
//		ArrayList allProps = new ArrayList();
//		ArrayList maskProps = new ArrayList();
//		ArrayList tempProps = new ArrayList();
//		int counter = 0;
//		int counter2 = 0;
//
//		if (null != iRuneWordProps) {
//			for (int a = 0; a < iRuneWordProps.size(); a = a + 1) {
//				maskProps.add("0");
//			}
//			allProps.addAll(iRuneWordProps);
//			iRuneWordProps.clear();
//		}
//		if (null != iGemProps) {
//			for (int a = 0; a < iGemProps.size(); a = a + 1) {
//				maskProps.add("1");
//			}
//			allProps.addAll(iGemProps);
//			iGemProps.clear();
//		}
//		if (null != iProperties) {
//			for (int a = 0; a < iProperties.size(); a = a + 1) {
//				maskProps.add("2");
//			}
//			allProps.addAll(iProperties);
//			iProperties.clear();
//		}
//
//		for (int x = 0; x < allProps.size(); x = x + 1) {
//			for (int y = 0; y < allProps.size(); y = y + 1) {
//				if (((D2ItemProperty) allProps.get(x)).getiProp() == ((D2ItemProperty) allProps
//						.get(y)).getiProp()) {
//					if (!((D2ItemProperty) allProps.get(x))
//							.equals((D2ItemProperty) allProps.get(y))
//							&& ((D2ItemProperty) allProps.get(x)).getiProp() != 107
//							&& ((D2ItemProperty) allProps.get(x)).getiProp() != 97
//							&& ((D2ItemProperty) allProps.get(x)).getiProp() != 188
//							&& ((D2ItemProperty) allProps.get(x)).getiProp() != 201
//							&& ((D2ItemProperty) allProps.get(x)).getiProp() != 198
//							&& ((D2ItemProperty) allProps.get(x)).getiProp() != 204) {
//						counter = counter
//						+ ((D2ItemProperty) allProps.get(y))
//						.getRealValue();
//						if (((D2ItemProperty) allProps.get(x)).getiProp() == 48
//								|| ((D2ItemProperty) allProps.get(x))
//								.getiProp() == 50
//								|| ((D2ItemProperty) allProps.get(x))
//								.getiProp() == 52
//								|| ((D2ItemProperty) allProps.get(x))
//								.getiProp() == 54
//								|| ((D2ItemProperty) allProps.get(x))
//								.getiProp() == 57) {
//							counter2 = counter2
//							+ ((D2ItemProperty) allProps.get(y))
//							.getRealValueTwo();
//						}
//						// System.out.println("PROPERTY TO COMBINE FOUND: VAL:"+ ((D2ItemProperty) allProps.get(x)).getiProp() + ", " + ((D2ItemProperty) allProps.get(x)).getRealValue() + ", " + ",ITEM: "+this.iItemName);
//						tempProps.add(new Integer(y));
//					}
//				}
//			}
//			for (int b = 0; b < tempProps.size(); b = b + 1) {
//				allProps.remove(((Integer) tempProps.get(b)).intValue() - b);
//				maskProps.remove(((Integer) tempProps.get(b)).intValue() - b);
//			}
//			if (counter != 0) {
//				((D2ItemProperty) allProps.get(x))
//				.setRealValue(((D2ItemProperty) allProps.get(x))
//						.getRealValue()
//						+ counter);
//			}
//			if (counter2 != 0) {
//				((D2ItemProperty) allProps.get(x))
//				.setRealValueTwo(((D2ItemProperty) allProps.get(x))
//						.getRealValueTwo()
//						+ counter2);
//			}
//			counter = 0;
//			counter2 = 0;
//			tempProps.clear();
//		}
//
//		for (int c = 0; c < maskProps.size(); c = c + 1) {
//			if (maskProps.get(c).equals("0")) {
//				iRuneWordProps.add(allProps.get(c));
//			} else if (maskProps.get(c).equals("1")) {
//				iGemProps.add(allProps.get(c));
//			} else if (maskProps.get(c).equals("2")) {
//				iProperties.add(allProps.get(c));
//			}
//		}
//		if(iItemName.startsWith("Chain")){
////			System.out.println(((D2ItemProperty)iGemProps.get(1)).getRealValue());
//		}
//	}

//	private void combineResists() {
//
//		ArrayList allProps = new ArrayList();
//		ArrayList maskProps = new ArrayList();
//		ArrayList tempProps = new ArrayList();
//		ArrayList tempStatProps = new ArrayList();
//		ArrayList tempMaxResProps = new ArrayList();
//		ArrayList maxResistanceKeys = new ArrayList();
//		ArrayList resistanceKeys = new ArrayList();
//		ArrayList statKeys = new ArrayList();
//		resistanceKeys.add(new Integer(39));
//		resistanceKeys.add(new Integer(41));
//		resistanceKeys.add(new Integer(43));
//		resistanceKeys.add(new Integer(45));
//
//		maxResistanceKeys.add(new Integer(40));
//		maxResistanceKeys.add(new Integer(42));
//		maxResistanceKeys.add(new Integer(44));
//		maxResistanceKeys.add(new Integer(46));
//
//		statKeys.add(new Integer(0));
//		statKeys.add(new Integer(1));
//		statKeys.add(new Integer(2));
//		statKeys.add(new Integer(3));
//
//		int smallestRes = 0;
//		int smallestMaxRes = 0;
//		int smallestStat = 0;
//
//		if (null != iRuneWordProps) {
//			for (int a = 0; a < iRuneWordProps.size(); a = a + 1) {
//				maskProps.add("0");
//			}
//			allProps.addAll(iRuneWordProps);
//			iRuneWordProps.clear();
//		}
//		if (null != iGemProps) {
//			if (!isGem() && !isRune()) {
//				for (int a = 0; a < iGemProps.size(); a = a + 1) {
//					maskProps.add("1");
//				}
//				allProps.addAll(iGemProps);
//				iGemProps.clear();
//			} else {
//				for (int a = 0; a < ((ArrayList) iGemProps.get(2)).size(); a = a + 1) {
//					maskProps.add("1");
//				}
//				allProps.addAll(((ArrayList) iGemProps.get(2)));
//				((ArrayList) iGemProps.get(2)).clear();
//			}
//
//		}
//		if (null != iProperties) {
//			for (int a = 0; a < iProperties.size(); a = a + 1) {
//				maskProps.add("2");
//			}
//			allProps.addAll(iProperties);
//			iProperties.clear();
//		}
//
//		for (int x = 0; x < allProps.size(); x = x + 1) {
//			// CHECK REQUIREMENTS FIRST
//			if (((D2ItemProperty) allProps.get(x)).getiProp() == 91) {
//				modifyReqs(((D2ItemProperty) allProps.get(x)).getRealValue());
//			}
//			// ...
//			if (resistanceKeys.contains(new Integer(((D2ItemProperty) allProps
//					.get(x)).getiProp()))) {
//				resistanceKeys.remove(new Integer(((D2ItemProperty) allProps
//						.get(x)).getiProp()));
//				tempProps.add(((D2ItemProperty) allProps.get(x)));
//				for (int y = 0; y < allProps.size(); y = y + 1) {
//					if (resistanceKeys.contains(new Integer(
//							((D2ItemProperty) allProps.get(y)).getiProp()))) {
//						resistanceKeys.remove(new Integer(
//								((D2ItemProperty) allProps.get(y)).getiProp()));
//						tempProps.add(((D2ItemProperty) allProps.get(y)));
//					}
//				}
//			}
//			if (maxResistanceKeys.contains(new Integer(
//					((D2ItemProperty) allProps.get(x)).getiProp()))) {
//				maxResistanceKeys.remove(new Integer(((D2ItemProperty) allProps
//						.get(x)).getiProp()));
//				tempMaxResProps.add(((D2ItemProperty) allProps.get(x)));
//				for (int y = 0; y < allProps.size(); y = y + 1) {
//					if (maxResistanceKeys.contains(new Integer(
//							((D2ItemProperty) allProps.get(y)).getiProp()))) {
//						maxResistanceKeys.remove(new Integer(
//								((D2ItemProperty) allProps.get(y)).getiProp()));
//						tempMaxResProps.add(((D2ItemProperty) allProps.get(y)));
//					}
//				}
//			}
//
//			if (statKeys.contains(new Integer(
//					((D2ItemProperty) allProps.get(x)).getiProp()))) {
//				statKeys.remove(new Integer(((D2ItemProperty) allProps.get(x))
//						.getiProp()));
//				tempStatProps.add(((D2ItemProperty) allProps.get(x)));
//				for (int y = 0; y < allProps.size(); y = y + 1) {
//					if (statKeys.contains(new Integer(
//							((D2ItemProperty) allProps.get(y)).getiProp()))) {
//						statKeys.remove(new Integer(((D2ItemProperty) allProps
//								.get(y)).getiProp()));
//						tempStatProps.add(((D2ItemProperty) allProps.get(y)));
//					}
//				}
//			}
//		}
//
//		if (maxResistanceKeys.size() == 0) {
//			smallestMaxRes = ((D2ItemProperty) tempMaxResProps.get(0))
//			.getRealValue();
//			for (int f = 1; f < tempMaxResProps.size(); f = f + 1) {
//				if (((D2ItemProperty) tempMaxResProps.get(f)).getRealValue() < smallestMaxRes) {
//					smallestMaxRes = ((D2ItemProperty) tempMaxResProps.get(f))
//					.getRealValue();
//				}
//			}
//
//			for (int f = 0; f < tempMaxResProps.size(); f = f + 1) {
//				((D2ItemProperty) tempMaxResProps.get(f))
//				.setRealValue(((D2ItemProperty) tempMaxResProps.get(f))
//						.getRealValue()
//						- smallestMaxRes);
//				if (((D2ItemProperty) tempMaxResProps.get(f)).getRealValue() == 0) {
//					maskProps.remove(allProps.indexOf(tempMaxResProps.get(f)));
//					allProps.remove(tempMaxResProps.get(f));
//				}
//			}
//		}
//
//		if (resistanceKeys.size() == 0) {
//			smallestRes = ((D2ItemProperty) tempProps.get(0)).getRealValue();
//			for (int f = 1; f < tempProps.size(); f = f + 1) {
//				if (((D2ItemProperty) tempProps.get(f)).getRealValue() < smallestRes) {
//					smallestRes = ((D2ItemProperty) tempProps.get(f))
//					.getRealValue();
//				}
//			}
//
//			for (int f = 0; f < tempProps.size(); f = f + 1) {
//				((D2ItemProperty) tempProps.get(f))
//				.setRealValue(((D2ItemProperty) tempProps.get(f))
//						.getRealValue()
//						- smallestRes);
//				if (((D2ItemProperty) tempProps.get(f)).getRealValue() == 0) {
//					maskProps.remove(allProps.indexOf(tempProps.get(f)));
//					allProps.remove(tempProps.get(f));
//				}
//			}
//		}
//
//		if (statKeys.size() == 0) {
//			smallestStat = ((D2ItemProperty) tempStatProps.get(0))
//			.getRealValue();
//			for (int f = 1; f < tempStatProps.size(); f = f + 1) {
//				if (((D2ItemProperty) tempStatProps.get(f)).getRealValue() < smallestStat) {
//					smallestStat = ((D2ItemProperty) tempStatProps.get(f))
//					.getRealValue();
//				}
//			}
//
//			for (int f = 0; f < tempStatProps.size(); f = f + 1) {
//				((D2ItemProperty) tempStatProps.get(f))
//				.setRealValue(((D2ItemProperty) tempStatProps.get(f))
//						.getRealValue()
//						- smallestStat);
//				if (((D2ItemProperty) tempStatProps.get(f)).getRealValue() == 0) {
//					maskProps.remove(allProps.indexOf(tempStatProps.get(f)));
//					allProps.remove(tempStatProps.get(f));
//				}
//			}
//		}
//
//		// for (int b = 0; b < tempProps.size(); b = b + 1) {
//		// allProps.remove(((Integer) tempProps.get(b)).intValue() -b);
//		// maskProps.remove(((Integer) tempProps.get(b)).intValue() - b);
//		// }
//		// if (counter != 0) {
//		// ((D2ItemProperty) allProps.get(x))
//		// .setRealValue(((D2ItemProperty) allProps.get(x))
//		// .getRealValue()
//		// + counter);
//		// }
//		// counter = 0;
//		// tempProps.clear();
//		// }
//		//
//		if (!isGem() && !isRune()) {
//			for (int c = 0; c < maskProps.size(); c = c + 1) {
//				if (maskProps.get(c).equals("0")) {
//					iRuneWordProps.add(allProps.get(c));
//				} else if (maskProps.get(c).equals("1")) {
//					iGemProps.add(allProps.get(c));
//				} else if (maskProps.get(c).equals("2")) {
//					iProperties.add(allProps.get(c));
//				}
//			}
//
//		} else {
//
//			for (int c = 0; c < maskProps.size(); c = c + 1) {
//				if (maskProps.get(c).equals("0")) {
//					iRuneWordProps.add(allProps.get(c));
//				} else if (maskProps.get(c).equals("1")) {
//					((ArrayList) iGemProps.get(2)).add(allProps.get(c));
//				} else if (maskProps.get(c).equals("2")) {
//					iProperties.add(allProps.get(c));
//				}
//
//			}
//		}
//
//		if (resistanceKeys.size() == 0) {
//			// System.out.println(iItemName + " HAS HAD RESISTANCES COMBINED");
//
//			D2ItemProperty lProperty = new D2ItemProperty(1337, iCharLvl,
//					iItemName);
//
//			D2TxtFileItemProperties lItemStatCost2 = null;
//			lProperty.set(1337, lItemStatCost2, 0, smallestRes);
//			if (!isGem() && !isRune()) {
//				iProperties.add(lProperty);
//
//				combineProps();
//
//			} else {
//				((ArrayList) iGemProps.get(2)).add(lProperty);
//			}
//		}
//
//		if (statKeys.size() == 0) {
//			// System.out.println(iItemName + " HAS HAD STATS COMBINED");
//			D2ItemProperty lProperty = new D2ItemProperty(1338, iCharLvl,
//					iItemName);
//			D2TxtFileItemProperties lItemStatCost2 = null;
//			lProperty.set(1338, lItemStatCost2, 0, smallestStat);
//			if (!isGem() && !isRune()) {
//				iProperties.add(lProperty);
//				combineProps();
//			} else {
//				((ArrayList) iGemProps.get(2)).add(lProperty);
//			}
//		}
//
//		if (maxResistanceKeys.size() == 0) {
//			// System.out.println(iItemName + " HAS HAD MAX RESISTANCES
//			// COMBINED");
//			D2ItemProperty lProperty = new D2ItemProperty(1339, iCharLvl,
//					iItemName);
//			D2TxtFileItemProperties lItemStatCost2 = null;
//			lProperty.set(1339, lItemStatCost2, 0, smallestMaxRes);
//			if (!isGem() && !isRune()) {
//				iProperties.add(lProperty);
//				combineProps();
//			} else {
//				((ArrayList) iGemProps.get(2)).add(lProperty);
//			}
//		}
//
//
//	}

	private void modifyReqs(int value) {
//		System.out.println(iItemName);
		if (getReqDex() != -1) {
			iReqDex = iReqDex + ((int)(iReqDex*((double) value / (double) 100)));
			// iReqDex = (int)Math.floor((iReqDex
			// +(((double)iReqDex/(double)100)*value)));

			// System.out.println(iItemName + "HAS HAD DEXTERITY CHANGED");
		}

		if (getReqStr() != -1) {
			// iReqStr = (int)(iReqStr +(((double)iReqStr/(double)100)*value));
			iReqStr = iReqStr
			+ ((int) (iReqStr
					* ((double) value / (double) 100)));
			// System.out.println(iItemName + " HAS HAD STRENGTH CHANGED");
		}
	}

	public boolean isBodyLArm() {
		return isBodyLocation("larm");
	}

	public boolean isBodyRRin() {
		return isBodyLocation("rrin");
	}

	public boolean isBodyLRin() {
		return isBodyLocation("lrin");
	}

	public boolean isWeaponType(D2WeaponTypes pType) {
		if (iTypeWeapon) {
			if (pType.isType(iType)) {
				return true;
			}
		}
		return false;
	}

	public boolean isBodyLocation(D2BodyLocations pLocation) {
		if (iBody) {
			if (pLocation.getLocation().equals(iBodyLoc1)) {
				return true;
			}
			if (pLocation.getLocation().equals(iBodyLoc2)) {
				return true;
			}
		}
		return false;
	}

	private boolean isBodyLocation(String pLocation) {
		if (iBody) {
			if (pLocation.equals(iBodyLoc1)) {
				return true;
			}
			if (pLocation.equals(iBodyLoc2)) {
				return true;
			}
		}
		return false;
	}

	public boolean isBelt() {
		return iBelt;
	}

	public boolean isCharm() {
		return (iSmallCharm || iLargeCharm || iGrandCharm);
	}

	public boolean isCharmSmall() {
		return iSmallCharm;
	}

	public boolean isCharmLarge() {
		return iLargeCharm;
	}

	public boolean isCharmGrand() {
		return iGrandCharm;
	}

	public boolean isJewel() {
		return iJewel;
	}

	// accessor for the row
	public short get_row() {
		return row;
	}

	// accessor for the column
	public short get_col() {
		return col;
	}

	// setter for the row
	// necessary for moving items
	public void set_row(short r) {
		iItem.set_byte_pos(7);
		iItem.skipBits(13);
		iItem.write((long) r, 4);
		row = r;
	}

	// setter for the column
	// necessary for moving items
	public void set_col(short c) {
		iItem.set_byte_pos(7);
		iItem.skipBits(9);
		iItem.write((long) c, 4);
		col = c;
	}

	public void set_location(short l) {
		iItem.set_byte_pos(7);
		iItem.skipBits(2);
		iItem.write((long) l, 3);
		location = l;
	}

	public void set_body_position(short bp) {
		iItem.set_byte_pos(7);
		iItem.skipBits(5);
		iItem.write((long) bp, 4);
		body_position = bp;
	}

	public void set_panel(short p) {
		iItem.set_byte_pos(7);
		iItem.skipBits(17);
		iItem.write((long) p, 3);
		panel = p;
	}

	public short get_location() {
		return location;
	}

	public short get_body_position() {
		return body_position;
	}

	public short get_panel() {
		return panel;
	}

	public short get_width() {
		return width;
	}

	public short get_height() {
		return height;
	}

	public String get_image() {
		return image_file;
	}

	public String get_name() {
		return name;
	}

	public String get_version() {

		if (version == 0) {
			return "Legacy (pre 1.08)";
		}

		if (version == 1) {
			return "Classic";
		}

		if (version == 100) {
			return "Expansion";
		}

		if (version == 101) {
			return "Expansion 1.10+";
		}

		return "UNKNOWN";
	}

	public long getSocketNrFilled() {
		return iSocketNrFilled;
	}

	public long getSocketNrTotal() {
		return iSocketNrTotal;
	}

	public byte[] get_bytes() {
		return iItem.getFileContent();
	}

	public int getItemLength() {
		return iItem.get_length();
	}

	public String getItemName() {
		return iItemName;
	}

	public String getName() {
		return iItemNameNoPersonalising;
	}

	public String getFingerprint() {
		return iFP;
	}

	public String getILvl() {
		return Short.toString(ilvl);
	}

	public String toString(int disSepProp) {
		ArrayList lDump = getFullItemDump(1, disSepProp);
		StringBuffer lReturn = new StringBuffer("");
		for (int i = 0; i < lDump.size(); i++) {
			if (i > 0 && !lDump.get(i).equals("")) {
				lReturn.append("\n");
			}
			lReturn.append((String) lDump.get(i));
		}
		return lReturn.toString();
	}

	public void toWriter(PrintWriter pWriter) {
		ArrayList lDump = getFullItemDump(1, 0);
		for (int i = 0; i < lDump.size(); i++) {
			pWriter.println((String) lDump.get(i));
		}
		pWriter.println();
	}

	public String toStringHtml(int stash, int disSepProp) {
		ArrayList lDump = getFullItemDump(stash, disSepProp);
		StringBuffer lReturn = new StringBuffer("<HTML>");
		for (int i = 0; i < lDump.size(); i++) {
			if (i > 0) {
				lReturn.append("<BR>");
			}
			lReturn.append((String) lDump.get(i));
		}
		return lReturn.toString();
	}

	private ArrayList getProperties(String pProps, ArrayList pProperties) {
		ArrayList lReturn = new ArrayList();

		if (pProperties != null) {
			if (pProps != null) {
				lReturn.add(pProps);
			}
			for (int i = 0; i < pProperties.size(); i++) {
				D2ItemProperty lValue = (D2ItemProperty) pProperties.get(i);
				if (!lValue.hasNoValue()) {
					String lText = lValue.getValue();
					if (lText != null) {
						if(((D2ItemProperty) pProperties.get(i)).getItemName().equals("Um Rune")){
							if(this.iItemName.equals("Um Rune")){
								//System.out.println(this.iItemName);
							}
						}


						lReturn.add(lText);
					}
				}
			}
		}

		return lReturn;
	}

	public int getReqLvl() {
		return iReqLvl;
	}

	public int getReqStr() {
		return iReqStr;
	}

	public int getReqDex() {
		return iReqDex;
	}

	public ArrayList getFullItemDump(int stash, int disSepProp) {
		ArrayList lReturn = new ArrayList();

		String base = Integer.toHexString(Color.white.getRGB());
		base = base.substring(2, base.length());

		String rgb = Integer.toHexString(getItemColor().getRGB());
		rgb = rgb.substring(2, rgb.length());
		// System.out.println(rgb);
		if (stash == 1) {

			if (personalization == null) {
				lReturn.add(iItemName);
			}else{
				lReturn.add(personalization + "'s " + iItemName);
			}


			if (!iBaseItemName.equals(iItemName)) {
				lReturn.add(iBaseItemName);
			}
		} else {
			if (disSepProp == 1) {

				if (personalization == null) {
					lReturn.add("<font face=\"Dialog\" size=\"3\" color=\"#" + base
							+ "\">" + "<font color=\"#" + rgb + "\">" + iItemName
							+ "</font>");
				}else{

					lReturn.add("<font face=\"Dialog\" size=\"3\" color=\"#" + base
							+ "\">" + "<font color=\"#" + rgb + "\">" + personalization + "'s " + iItemName
							+ "</font>");

				}


			} else {

				if (personalization == null) {
					lReturn.add("<font color=\"#" + base + "\">"
							+ "<font color=\"#" + rgb + "\">" + iItemName
							+ "</font>");
				}else{
					lReturn.add("<font color=\"#" + base + "\">"
							+ "<font color=\"#" + rgb + "\">" + personalization + "'s " + iItemName
							+ "</font>");
				}

			}
			if (!iBaseItemName.equals(iItemName)) {
				if (!isRuneWord()) {
					lReturn.add("<font color=\"#" + rgb + "\">" + iBaseItemName
							+ "</font>");
				} else {
					rgb = Integer.toHexString(Color.gray.getRGB());
					rgb = rgb.substring(2, rgb.length());
					lReturn.add("<font color=\"#" + rgb + "\">" + iBaseItemName
							+ "</font>");

				}
			}
		}
		if (isTypeWeapon()) {
			if (iWhichHand == 0) {
				if (iThrow) {
					lReturn.add("Throw Damage: " + i2MinDmg + " - " + i2MaxDmg);
					lReturn
					.add("One Hand Damage: " + iMinDmg + " - "
							+ iMaxDmg);

				} else {
					lReturn
					.add("One Hand Damage: " + iMinDmg + " - "
							+ iMaxDmg);
					lReturn.add("Two Hand Damage: " + i2MinDmg + " - "
							+ i2MaxDmg);
				}
			} else {
				if (iWhichHand == 1) {
					lReturn
					.add("One Hand Damage: " + iMinDmg + " - "
							+ iMaxDmg);
				} else {
					lReturn
					.add("Two Hand Damage: " + iMinDmg + " - "
							+ iMaxDmg);
				}
			}
		} else if (isTypeArmor()) {
			lReturn.add("Defense: " + iDef);
		}

		if(isTypeArmor()){
			if(iType.equals("ashd")||iType.equals("shie")||iType.equals("head")){
				lReturn.add("Chance to Block: " + iBlock);
			}
		}

		if (isTypeWeapon() || isTypeArmor()) {
			if (isStackable()) {
				lReturn.add("Quantity: " + iCurDur);
			} else {
				if (iMaxDur == 0) {
					lReturn.add("Indestructible");
				} else {
					lReturn.add("Durability: " + iCurDur + " of " + iMaxDur);
				}
			}
		}

		if (iReqLvl > 0) {
			lReturn.add("Required Level: " + iReqLvl);
		}
		if (iReqStr > 0) {
			lReturn.add("Required Strength: " + iReqStr);
		}
		if (iReqDex > 0) {
			lReturn.add("Required Dexterity: " + iReqDex);
		}

		if (iFP != null) {
			lReturn.add("Fingerprint: " + iFP);
		}
		if (hasGUID) {
			lReturn.add("GUID: " + iGUID);
		}

		if (ilvl != 0) {
			lReturn.add("Item Level: " + ilvl);
		}

		lReturn.add("Version: " + get_version());
		if (!iIdentified) {
			lReturn.add("Unidentified");
		}
		lReturn.add("properties: ");
		lReturn.addAll(iProps.generateDisplay());
//		lReturn.addAll(getProperties("Properties: ", iProperties));
		if (isGem() || isRune()) {
			lReturn.addAll(getProperties("Weapons: ", (ArrayList) iGemProps
					.get(0)));
			lReturn.addAll(getProperties("Armor: ", (ArrayList) iGemProps
					.get(1)));
			lReturn.addAll(getProperties("Shields: ", (ArrayList) iGemProps
					.get(2)));
		}

		if (iSet1 != null && iSet1.size() > 0)
			lReturn.addAll(getProperties("Set (2 item): ", iSet1));
		if (iSet2 != null && iSet2.size() > 0)
			lReturn.addAll(getProperties("Set (3 items): ", iSet2));
		if (iSet3 != null && iSet3.size() > 0)
			lReturn.addAll(getProperties("Set (4 items): ", iSet3));
		if (iSet4 != null && iSet4.size() > 0)
			lReturn.addAll(getProperties("Set (5 items): ", iSet4));
		if (iSet5 != null && iSet5.size() > 0)
			lReturn.addAll(getProperties("Set (?? items): ", iSet5));

		if (iRuneWord) {
			lReturn.addAll(getProperties(null, iRuneWordProps));
		}

		if (iEthereal) {
			lReturn.add("Ethereal");
		}
		if (iSocketNrTotal > 0) {
			if (iGemProps.size() > 0) {
				// lReturn += lSocket.getProperties(null,
				// lSocket.iProperties);
				lReturn.addAll(getProperties(null, iGemProps));
			}
			lReturn.add(iSocketNrTotal + " Sockets (" + iSocketNrFilled
					+ " used)");
			if (iSocketedItems != null) {
				for (int i = 0; i < iSocketedItems.size(); i++) {
					D2Item lSocket = ((D2Item) iSocketedItems.get(i));
					lReturn.add("Socketed: " + lSocket.getItemName());
					// if (lSocket.isJewel()) {
					// lReturn += lSocket.getProperties(null,
					// lSocket.iProperties);
					// }

				}
			}
		}

		if (disSepProp == 1) {

			if (isSocketed()) {
				lReturn.add("");
				if (stash == 1) {
					if (iSocketedItems != null) {
						for (int x = 0; x < iSocketedItems.size(); x = x + 1) {
							if (((D2Item) iSocketedItems.get(x)) != null) {
								lReturn.add(((D2Item) iSocketedItems.get(x))
										.toString(0)
										+ "\n");
							}
						}
					}
				} else {
					if (iSocketedItems != null) {
						for (int x = 0; x < iSocketedItems.size(); x = x + 1) {

							lReturn.add(((D2Item) iSocketedItems.get(x))
									.toStringHtml(stash, 0));
						}
					}
				}

			}

		}

		if (stash == 0) {
			lReturn.add("</font>");
		}

		return lReturn;
	}

	public Color getItemColor() {
		if (isUnique()) {
			// return Color.yellow.darker().darker();
			return new Color(255, 222, 173);
		}
		if (isSet()) {
			return Color.green.darker();
		}
		if (isRare()) {
			return Color.yellow.brighter();
		}
		if (isMagical()) {
			return new Color(72, 118, 255);
		}
		if (isRune()) {
			return Color.red;
		}
		if (isCrafted()) {
			return Color.orange;
		}
		if (isRuneWord()) {
			return new Color(255, 222, 173);
		}
		if (isEthereal() || isSocketed()) {
			return Color.gray;
		}
		return Color.white;
	}

	// private String socketedGemHandler(D2Item lSocket) {
	//
	// String propsReturned = "";
	//
	// // System.out.println(iType);
	// if (iType.equals("scep") || iType.equals("wand")
	// || iType.equals("staf") || iType.equals("bow")
	// || iType.equals("axe") || iType.equals("club")
	// || iType.equals("hamm") || iType.equals("swor")
	// || iType.equals("knif") || iType.equals("spea")
	// || iType.equals("pole") || iType.equals("orb")
	// || iType.equals("xbow") || iType.equals("mace")
	// || iType.equals("h2h") || iType.equals("abow")
	// || iType.equals("aspe") || iType.equals("jave")
	// || iType.equals("ajav") || iType.equals("h2h2")
	// || iType.equals("tkni") || iType.equals("taxe")) {
	//
	// // propsNotParsed = lSocket.getProperties(null,
	// // lSocket.iProperties).split("Weapons: ");
	//
	// propsReturned = propsReturned
	// + getProperties(null, (ArrayList) lSocket.iGemProps.get(0));
	// // System.out.println(propsNotParsed.length);
	// // System.out.println(propsNotParsed[0]);
	// // System.out.println(propsNotParsed[1]);
	// // System.out.println(propsNotParsed[2]);
	//
	// } else if (iType.equals("tors") || iType.equals("helm")
	// || iType.equals("phlm") || iType.equals("pelt")
	// || iType.equals("cloa") || iType.equals("circ")) {
	//
	// // propsNotParsed = lSocket.getProperties(null,
	// // lSocket.iProperties).split("Armour: ");
	// propsReturned = propsReturned
	// + getProperties(null, (ArrayList) lSocket.iGemProps.get(1));
	// ;
	// // System.out.println(propsNotParsed.length);
	// // System.out.println(propsNotParsed[0]);
	// // System.out.println(propsNotParsed[1]);
	// // System.out.println(propsNotParsed[2]);
	//
	// } else {
	// // System.out.println(iGemProps[1]);
	// // for(int x = 0;
	// // x<((ArrayList)lSocket.iGemProps.get(2)).size();x=x+1){
	// propsReturned = propsReturned
	// + getProperties(null, (ArrayList) lSocket.iGemProps.get(2));
	// // }
	// // propsNotParsed = lSocket.getProperties(null,
	// // lSocket.iProperties).split("Shields: ");
	//
	// }
	//
	// // System.out.println("PROPS " +propsReturned);
	// return propsReturned;
	// }

	// public boolean isCharacter()
	// {
	// return iCharacter;
	// }

	public boolean isUnique() {
		return iUnique;
	}

	public boolean isSet() {
		return iSet;
	}

	public boolean isRuneWord() {
		return iRuneWord;
	}

	public boolean isCrafted() {
		return iCrafted;
	}

	public boolean isRare() {
		return iRare;
	}

	public boolean isMagical() {
		return iMagical;
	}

	public boolean isNormal() {
		return !(iMagical || iRare || iCrafted || iRuneWord || isRune() || iSet || iUnique);
	}

	public boolean isSocketFiller() {
		return isRune() || isJewel() || isGem();
	}

	public boolean isGem() {
		return iGem;
	}

	public boolean isRune() {
		return getRuneCode() != null;
	}

	public String getRuneCode() {
		if (iItemType != null) {
			if ("rune".equals(iItemType.get("type"))) {
				return iItemType.get("code");
			}
		}
		return null;
	}

	public boolean isEthereal() {
		return iEthereal;
	}

	public boolean isSocketed() {
		return iSocketed;
	}

	public boolean isStackable() {
		return iStackable;
	}

	public boolean isTypeMisc() {
		return iTypeMisc;
	}

	public boolean isTypeArmor() {
		return iTypeArmor;
	}

	public boolean isTypeWeapon() {
		return iTypeWeapon;
	}

	public void setCharLvl(long pCharLvl) {
		iCharLvl = pCharLvl;
//		setCharLvl(iProperties, pCharLvl);
		setCharLvl(iSet1, pCharLvl);
		setCharLvl(iSet2, pCharLvl);
		setCharLvl(iSet3, pCharLvl);
		setCharLvl(iSet4, pCharLvl);
		setCharLvl(iSet5, pCharLvl);
	}

	private void setCharLvl(ArrayList pProperties, long pCharLvl) {
		if (pProperties != null) {
			for (int i = 0; i < pProperties.size(); i++) {
				((D2ItemProperty) pProperties.get(i)).setCharLvl(pCharLvl);
			}
		}
	}

	public boolean isCursorItem() {
		if (location != 0 && location != 2) {
			if (body_position == 0) {
				// System.err.println("location: " + location );
				return true;
			}
		}
		return false;
	}

	public int compareTo(Object pObject) {
		if (pObject instanceof D2Item) {
			String lItemName = ((D2Item) pObject).iItemName;
			if (iItemName == lItemName) {
				// also both "null"
				return 0;
			}
			if (iItemName == null) {
				return -1;
			}
			if (lItemName == null) {
				return 1;
			}
			return iItemName.compareTo(lItemName);
		}
		return -1;
	}

	public ArrayList getAllProps() {

//		ArrayList out = new ArrayList(iProperties);
//		if (iRuneWord) {
//			out.addAll(iRuneWordProps);
//		}
//		if (iSocketed) {
//			out.addAll(iGemProps);
//		}
//		if (iSet) {
//			out.addAll(iSetProps);
//		}
//		return out;
		return null;
	}

	public int getiDef() {
		return (int) iDef;
	}

	public boolean isEquipped() {

		if (get_location() == 1) {
			return true;
		} else if (get_panel() == 1 && isCharm()) {
			return true;
		} else {
			return false;
		}
	}

	public void setSetProps(int numItems) {
		iSetProps.clear();
		/*
		 * if(numItems == 0){ iSetProps.clear(); }else if(numItems == 1){
		 * iSetProps.clear();
		 * 
		 * 
		 * }else
		 */

		if (numItems == 2) {
			iSetProps.addAll(iSet1);
		} else if (numItems == 3) {
			iSetProps.addAll(iSet1);
			iSetProps.addAll(iSet2);
		} else if (numItems == 4) {
			iSetProps.addAll(iSet1);
			iSetProps.addAll(iSet2);
			iSetProps.addAll(iSet3);
		} else if (numItems == 5) {
			iSetProps.addAll(iSet1);
			iSetProps.addAll(iSet2);
			iSetProps.addAll(iSet3);
			iSetProps.addAll(iSet4);
		}
		// for(int x =0;x<iSetProps.size();x=x+1){
		// System.out.println(numItems + " VAL:
		// "+((D2ItemProperty)iSetProps.get(x)).getValue());
		// }

		if (isTypeArmor()) {
//			applyEDef();

		}
	}

	public int getSetID() {
		return (int) set_id;
	}

	public int getSetSize() {
		return setSize;
	}

	public String getSetName() {
		return iSetName;
	}

	public boolean statModding() {

		if (iJewel || iGem || iRune) {
			return false;
		} else {

			return true;
		}
	}

	public ArrayList getPerfectStringUS() {
		D2ItemProperty[] outProp;
		ArrayList outArrL = new ArrayList();
		D2TxtFileItemProperties pRow;
		if (this.isUnique()) {
			pRow = D2TxtFile.UNIQUES.getRow(unique_id);
		} else if (this.isSet()) {
			pRow = D2TxtFile.SETITEMS.getRow(set_id);
		} else {
			pRow = D2TxtFile.RUNES.getRow(runeword_id);
		}

		int counter = 1;
		int max = 13;
		if (this.isSet()) {
			max = 10;
		} else if (this.isRuneWord()) {
			max = 8;
		}
		String prop = "prop";

		if (isRuneWord()) {
			prop = "T1Code";
		}

		while (counter < max) {
			outProp = new D2ItemProperty[2];
			if (!pRow.get(prop + counter).equals("")) {

				if (pRow.get(prop + counter).indexOf("*") != -1) {
					break;
				}

//				System.out.println(pRow.get("prop" + counter) + " -- " +
//				pRow.get("min" + counter) + "-" + pRow.get("max" + counter));

				/*
				 * int lProp = Integer.parseInt((D2TxtFile.ITEM_STAT_COST
				 * .searchColumns("Stat", ((D2TxtFile.PROPS
				 * .searchColumns("code", (D2TxtFile.GEMS .searchColumns("code",
				 * item_type)) .get(interestingSubProp[x][y])))
				 * .get("stat1")))).get("ID"));
				 */

				int lProp = Integer.parseInt(D2TxtFile.ITEM_STAT_COST
						.searchColumns(
								"Stat",
								(D2TxtFile.PROPS.searchColumns("code", pRow
										.get(prop + counter)).get("stat1")))
										.get("ID"));
				if (pRow.get(prop + counter).equals("res-all")) {
					lProp = 1337;
				}



				if (lProp == 54 || lProp == 55 || lProp == 56) {
					D2ItemProperty lProperty = new D2ItemProperty(101010,
							iCharLvl, iItemName);
					lProperty.set(101010, null, 0, 0);
					outProp[0] = lProperty;
					outProp[1] = lProperty;
					outArrL.add(outProp);
					if(pRow.get("prop" + counter).equals("dmg-cold")){
						counter = counter + 1;
					}else{
						counter = counter + 3;
					}
					continue;
				}

				if (lProp == 48 || lProp == 49) {
					D2ItemProperty lProperty = new D2ItemProperty(101010,
							iCharLvl, iItemName);
					lProperty.set(101010, null, 0, 1);
					outProp[0] = lProperty;
					outProp[1] = lProperty;
					outArrL.add(outProp);
					if(pRow.get("prop" + counter).equals("dmg-fire")){
						counter = counter + 1;
					}else{
						counter = counter + 2;
					}
					continue;
				}

				if (lProp == 50 || lProp == 51) {
					D2ItemProperty lProperty = new D2ItemProperty(101010,
							iCharLvl, iItemName);
					lProperty.set(101010, null, 0, 2);
					outProp[0] = lProperty;
					outProp[1] = lProperty;
					outArrL.add(outProp);
					if(pRow.get("prop" + counter).equals("dmg-ltng")){
						counter = counter + 1;
					}else{
						counter = counter + 2;
					}
					continue;
				}

				if (lProp == 52 || lProp == 53) {
					D2ItemProperty lProperty = new D2ItemProperty(101010,
							iCharLvl, iItemName);
					lProperty.set(101010, null, 0, 3);
					outProp[0] = lProperty;
					outProp[1] = lProperty;
					outArrL.add(outProp);
					if(pRow.get("prop" + counter).equals("dmg-mag")){
						counter = counter + 1;
					}else{
						counter = counter + 2;
					}
					continue;
				}

				if (lProp == 57 || lProp == 58 || lProp == 59) {
					D2ItemProperty lProperty = new D2ItemProperty(101010,
							iCharLvl, iItemName);
					lProperty.set(101010, null, 0, 4);
					outProp[0] = lProperty;
					outProp[1] = lProperty;
					outArrL.add(outProp);
					if(pRow.get("prop" + counter).equals("dmg-pois")){
						counter = counter + 1;
					}else{
						counter = counter + 3;
					}
					continue;
				}

				int lPropTemp = lProp;
				D2ItemProperty lProperty = new D2ItemProperty(lPropTemp,
						iCharLvl, iItemName);

				D2TxtFileItemProperties lItemStatCost = D2TxtFile.ITEM_STAT_COST
				.getRow(lProperty.getPropNrs()[0]);

				String pMin = pRow.get("min" + counter);
				String pMax = pRow.get("max" + counter);
				String pParam = pRow.get("par" + counter);

				if (isRuneWord()) {
					pMin = pRow.get("T1Min" + counter);
					pMax = pRow.get("T1Max" + counter);
					pParam = pRow.get("T1Param" + counter);
				}


				if (pRow.get(prop + counter).equals("dmg-norm")) {

					lProperty = new D2ItemProperty(21,
							iCharLvl, iItemName);

					lItemStatCost = D2TxtFile.ITEM_STAT_COST
					.getRow(lProperty.getPropNrs()[0]);

					lProperty.set(lPropTemp, lItemStatCost, 0, Long
							.parseLong(pMin));

					outProp[0] = lProperty;
					outProp[1] = lProperty;
					outArrL.add(outProp);

					outProp = new D2ItemProperty[2];

					lProperty = new D2ItemProperty(22,
							iCharLvl, iItemName);

					lItemStatCost = D2TxtFile.ITEM_STAT_COST
					.getRow(lProperty.getPropNrs()[0]);

					lProperty.set(lPropTemp, lItemStatCost, 0, Long
							.parseLong(pMax));

					outProp[0] = lProperty;
					outProp[1] = lProperty;
					outArrL.add(outProp);
					counter ++;
					continue;



				}

				if (lPropTemp == 201 || lPropTemp == 197 || lPropTemp == 199
						|| lPropTemp == 195 || lPropTemp == 198
						|| lPropTemp == 196) {

					if (!pMax.equals("")) {
						lProperty.set(lPropTemp, lItemStatCost, 0, Long
								.parseLong(pMax));
					}

					if (!pParam.equals("")) {
						// if(lPropTemp==198){

						try {
							lProperty.set(lPropTemp, lItemStatCost, 0, Long
									.parseLong(D2TxtFile.SKILLS.searchColumns(
											"skill", pParam).get("Id")));
						} catch (NullPointerException e) {
							lProperty.set(lPropTemp, lItemStatCost, 0, Long
									.parseLong(pParam));
						}
						// }else{
						// lProperty.set(lPropTemp, lItemStatCost, 0,
						// Long.parseLong(pParam));
						// }
					}

					if (!pMin.equals("")) {
						lProperty.set(lPropTemp, lItemStatCost, 0, Long
								.parseLong(pMin));
					}
				} else {

					if (!pMin.equals("")) {
						if (lPropTemp == 204) {
							lProperty.set(lPropTemp, lItemStatCost, 0, Long
									.parseLong(pMax));
						} else if (lPropTemp == 107) {
							try {
								lProperty.set(lPropTemp, lItemStatCost, 0, Long
										.parseLong(D2TxtFile.SKILLS
												.searchColumns("skill", pParam)
												.get("Id")));
							} catch (NullPointerException e) {
								lProperty.set(lPropTemp, lItemStatCost, 0, Long
										.parseLong(pParam));
							}


						} else {
							lProperty.set(lPropTemp, lItemStatCost, 0, Long
									.parseLong(pMin));
						}
					}

					if (!pMax.equals("")) {
						if (lPropTemp == 204) {
							lProperty.set(lPropTemp, lItemStatCost, 0, Long
									.parseLong(pMin));
						} else if (lPropTemp == 107) {

						} else {
							lProperty.set(lPropTemp, lItemStatCost, 0, Long
									.parseLong(pMax));
						}
					}

					if (!pParam.equals("")) {

						if (lPropTemp == 204 ||lPropTemp == 97) {
							lProperty.set(lPropTemp, lItemStatCost, 0, Long
									.parseLong(D2TxtFile.SKILLS.searchColumns(
											"skill", pParam).get("Id")));

//							System.out.println(pParam);
//							System.out.println(Long
//							.parseLong(D2TxtFile.SKILLS.searchColumns(
//							"skill", pParam).get("Id")));

						} else if (lPropTemp == 107) {
							lProperty.set(lPropTemp, lItemStatCost, 0, Long
									.parseLong(pMin));
						} else {
							lProperty.set(lPropTemp, lItemStatCost, 0, Long
									.parseLong(pParam));
						}
					}
				}
				outProp[0] = lProperty;
				lPropTemp = lProp;

				lProperty = new D2ItemProperty(lPropTemp, iCharLvl, iItemName);

				lItemStatCost = D2TxtFile.ITEM_STAT_COST.getRow(lProperty
						.getPropNrs()[0]);

				pMin = pRow.get("min" + counter);
				pMax = pRow.get("max" + counter);
				pParam = pRow.get("par" + counter);

				if (isRuneWord()) {
					pMin = pRow.get("T1Min" + counter);
					pMax = pRow.get("T1Max" + counter);
					pParam = pRow.get("T1Param" + counter);
				}

				if (lPropTemp == 201 || lPropTemp == 197 || lPropTemp == 199
						|| lPropTemp == 195 || lPropTemp == 198
						|| lPropTemp == 196) {

					if (!pMax.equals("")) {
						lProperty.set(lPropTemp, lItemStatCost, 0, Long
								.parseLong(pMax));
					}

					if (!pParam.equals("")) {
						// if(lPropTemp==198){
						try {
							lProperty.set(lPropTemp, lItemStatCost, 0, Long
									.parseLong(D2TxtFile.SKILLS.searchColumns(
											"skill", pParam).get("Id")));
						} catch (NullPointerException e) {
							lProperty.set(lPropTemp, lItemStatCost, 0, Long
									.parseLong(pParam));
						}
						// }else{
						// lProperty.set(lPropTemp, lItemStatCost, 0,
						// Long.parseLong(pParam));
						// }
					}

					if (!pMin.equals("")) {
						lProperty.set(lPropTemp, lItemStatCost, 0, Long
								.parseLong(pMin));
					}
				} else {
					if (!pMax.equals("")) {
						if (lPropTemp == 107) {

						} else {
							lProperty.set(lPropTemp, lItemStatCost, 0, Long
									.parseLong(pMax));
						}
					}

					if (!pMin.equals("")) {
						if (lPropTemp == 107) {
							try {
								lProperty.set(lPropTemp, lItemStatCost, 0, Long
										.parseLong(D2TxtFile.SKILLS
												.searchColumns("skill", pParam)
												.get("Id")));
							} catch (NullPointerException e) {
								lProperty.set(lPropTemp, lItemStatCost, 0, Long
										.parseLong(pParam));
							}
						} else {
							lProperty.set(lPropTemp, lItemStatCost, 0, Long
									.parseLong(pMin));
						}
					}

					if (!pParam.equals("")) {
						if (lPropTemp == 204||lPropTemp == 97) {
							lProperty.set(lPropTemp, lItemStatCost, 0, Long
									.parseLong(D2TxtFile.SKILLS.searchColumns(
											"skill", pParam).get("Id")));
						} else if (lPropTemp == 107) {
							lProperty.set(lPropTemp, lItemStatCost, 0, Long
									.parseLong(pMax));
						} else {
							lProperty.set(lPropTemp, lItemStatCost, 0, Long
									.parseLong(pParam));
						}
					}
				}

				// System.out.println(lProperty.getValue());
				outProp[1] = lProperty;
				outArrL.add(outProp);

			}
			counter++;
		}

		// System.out.println(applyPerfDef(tempProp));
		return outArrL;
	}

	public String[] getPerfectDef(ArrayList outArrL) {
		ArrayList tempProp = new ArrayList();
		String[] out = new String[2];
		for (int x = 0; x < outArrL.size(); x++) {
			tempProp.add(((D2ItemProperty[]) outArrL.get(x))[0]);
		}
		out[1] = Long.toString(applyPerfDef(tempProp));
		tempProp = new ArrayList();
		for (int x = 0; x < outArrL.size(); x++) {
			tempProp.add(((D2ItemProperty[]) outArrL.get(x))[1]);
		}
		out[0] = Long.toString(applyPerfDef(tempProp));

		return out;
	}

	public String[] getPerfectDmg(ArrayList outArrL) {
		ArrayList tempProp = new ArrayList();
		String[] out = new String[2];
		for (int x = 0; x < outArrL.size(); x++) {
			tempProp.add(((D2ItemProperty[]) outArrL.get(x))[0]);
		}
		tempProp = applyPerfEDmg(tempProp);
		String outStr = "One Hand Damage: ";
		for (int x = 0; x < tempProp.size(); x = x + 1) {
			if (x == 0) {
				outStr = outStr + tempProp.get(x) + " - ";
			} else if (x == 1) {
				outStr = outStr + tempProp.get(x) + "\n";
			} else if (x == 2) {
				if (iThrow) {
					outStr = outStr + "Throw Damage: " + tempProp.get(x)
					+ " - ";
				} else {
					outStr = outStr + "Two Hand Damage: " + tempProp.get(x)
					+ " - ";
				}
			} else if (x == 3) {
				outStr = outStr + tempProp.get(x) + "\n";
			}
		}
		out[0] = outStr;
		tempProp = new ArrayList();
		for (int x = 0; x < outArrL.size(); x++) {
			tempProp.add(((D2ItemProperty[]) outArrL.get(x))[1]);
		}
		tempProp = applyPerfEDmg(tempProp);

		outStr = "One Hand Damage: ";
		for (int x = 0; x < tempProp.size(); x = x + 1) {
			if (x == 0) {
				outStr = outStr + tempProp.get(x) + " - ";
			} else if (x == 1) {
				outStr = outStr + tempProp.get(x) + "\n";
			} else if (x == 2) {
				if (iThrow) {
					outStr = outStr + "Throw Damage: " + tempProp.get(x)
					+ " - ";
				} else {
					outStr = outStr + "Two Hand Damage: " + tempProp.get(x)
					+ " - ";
				}
			} else if (x == 3) {
				outStr = outStr + tempProp.get(x) + "\n";
			}
		}
		out[1] = outStr;

		return out;
	}

	public ArrayList getPerfectString() {

		if (isUnique() || isSet()) {
			return sortStats(getPerfectStringUS());
		}else if(isRuneWord()){

			ArrayList perfArr = getPerfectStringUS();
			for(int x = 0;x<iGemProps.size();x=x+1){
				//D2ItemProperty[] tempP = {(D2ItemProperty)iGemProps.get(x),(D2ItemProperty)iGemProps.get(x)};
				//perfArr.add(tempP);
			}


			return sortStats(perfArr);
		}

		return null;
	}

	private ArrayList sortStats(ArrayList perfectStringUS) {

		int[] sortArr = new int[perfectStringUS.size()];
		ArrayList outSorted = new ArrayList();
		for (int x = 0; x < perfectStringUS.size(); x = x + 1) {
			sortArr[x] = (((D2ItemProperty[]) perfectStringUS.get(x))[0])
			.getiProp();

		}

		Arrays.sort(sortArr);

		for (int x = 0; x < sortArr.length; x = x + 1) {
			D2ItemProperty[] obj = findObjProp(sortArr[x], perfectStringUS);
			outSorted.add(x, obj);
			perfectStringUS.remove(obj);
		}

		return outSorted;
	}

	private D2ItemProperty[] findObjProp(int i, ArrayList perfectStringUS) {

		for (int x = 0; x < perfectStringUS.size(); x = x + 1) {
			if ((((D2ItemProperty[]) perfectStringUS.get(x))[0]).getiProp() == i) {
				return (D2ItemProperty[]) perfectStringUS.get(x);
			}

		}

		return null;
	}

	private ArrayList applyPerfEDmg(ArrayList iProperties) {

		int ENDam = 0;
		int ENMaxDam = 0;
		int MinDam = 0;
		int MaxDam = 0;
		ArrayList out = new ArrayList();

		for (int x = 0; x < iProperties.size(); x = x + 1) {
			if (((D2ItemProperty) iProperties.get(x)).getiProp() == 17) {
				ENDam = ENDam
				+ ((D2ItemProperty) iProperties.get(x)).getRealValue();
			}
			if (((D2ItemProperty) iProperties.get(x)).getiProp() == 21) {
				MinDam = MinDam
				+ ((D2ItemProperty) iProperties.get(x)).getRealValue();
			}
			if (((D2ItemProperty) iProperties.get(x)).getiProp() == 22) {
				MaxDam = MaxDam
				+ ((D2ItemProperty) iProperties.get(x)).getRealValue();
			}
			if (((D2ItemProperty) iProperties.get(x)).getiProp() == 219) {
				ENMaxDam = ENMaxDam
				+ (int) Math.floor((((D2ItemProperty) iProperties
						.get(x)).getRealValue() * 0.125)
						* iCharLvl);
			}
			if (((D2ItemProperty) iProperties.get(x)).getiProp() == 218) {
				MaxDam = MaxDam
				+ (int) Math.floor((((D2ItemProperty) iProperties
						.get(x)).getRealValue() * 0.125)
						* iCharLvl);
			}
		}

		out.add(String.valueOf((long) Math
				.floor((((double) iinitMinDmg / (double) 100) * ENDam)
						+ (iinitMinDmg + MinDam))));
		out
		.add(String
				.valueOf((long) Math
						.floor((((double) iinitMaxDmg / (double) 100) * (ENDam + ENMaxDam))
								+ (iinitMaxDmg + MaxDam))));

		if (iWhichHand == 0) {
			out.add(String.valueOf((long) Math
					.floor((((double) iinit2MinDmg / (double) 100) * ENDam)
							+ (iinit2MinDmg + MinDam))));
			out
			.add(String
					.valueOf((long) Math
							.floor((((double) iinit2MaxDmg / (double) 100) * (ENDam + ENMaxDam))
									+ (iinit2MaxDmg + MaxDam))));
		}

		return out;

	}

	private long applyPerfDef(ArrayList iProperties) {

		int ENDef = 0;
		int Def = 0;

		if (isSet()) {

			for (int x = 0; x < iSetProps.size(); x = x + 1) {
				if (((D2ItemProperty) iSetProps.get(x)).getiProp() == 16) {
					ENDef = ENDef
					+ ((D2ItemProperty) iSetProps.get(x))
					.getRealValue();
				}
				if (((D2ItemProperty) iSetProps.get(x)).getiProp() == 31) {
					Def = Def
					+ ((D2ItemProperty) iSetProps.get(x))
					.getRealValue();
				}
				if (((D2ItemProperty) iSetProps.get(x)).getiProp() == 214) {
					Def = Def
					+ (int) Math.floor((((D2ItemProperty) iSetProps
							.get(x)).getRealValue() * 0.125)
							* iCharLvl);
				}
			}

		}

		for (int x = 0; x < iProperties.size(); x = x + 1) {
			if (((D2ItemProperty) iProperties.get(x)).getiProp() == 16) {
				ENDef = ENDef
				+ ((D2ItemProperty) iProperties.get(x)).getRealValue();
			}
			if (((D2ItemProperty) iProperties.get(x)).getiProp() == 31) {
				Def = Def
				+ ((D2ItemProperty) iProperties.get(x)).getRealValue();
			}
			if (((D2ItemProperty) iProperties.get(x)).getiProp() == 214) {
				Def = Def
				+ (int) Math.floor((((D2ItemProperty) iProperties
						.get(x)).getRealValue() * 0.125)
						* iCharLvl);
			}
		}
		return (int) Math.floor((((double) iInitDef / (double) 100) * ENDef)
				+ (iInitDef + Def));

	}

	public String getPreSuf() {

		String retStr = "";
		for (int x = 0; x < rare_prefixes.length; x++) {

			if (rare_prefixes[x] > 1) {

				retStr = retStr
				+ D2TblFile.getString(D2TxtFile.PREFIX.getRow(
						rare_prefixes[x]).get("Name")) + " ";
			}
		}

		retStr = retStr + iBaseItemName + " ";

		for (int x = 0; x < rare_suffixes.length; x++) {

			if (rare_suffixes[x] > 1) {

				retStr = retStr
				+ D2TblFile.getString(D2TxtFile.SUFFIX.getRow(
						rare_suffixes[x]).get("Name")) + " ";
			}
		}

		return retStr;
	}

	public boolean conforms(String prop, int pVal, boolean min) {

//		System.out.println();
		ArrayList dumpStr = getFullItemDump(1, 0);

		for(int x = 0;x<dumpStr.size();x=x+1){
			if(((String)dumpStr.get(x)).toLowerCase().contains(prop.toLowerCase())){
//				System.out.println(dumpStr.get(x));

				if(pVal == -1337){
					return true;
				}

				Pattern pat = Pattern.compile("\\d+");
				Matcher mat = pat.matcher((String)dumpStr.get(x));

				while(mat.find()){



					if(min == true){
//						System.out.println(mat.group());
						if(Integer.parseInt(mat.group()) >= pVal){

							return true;
						}
					}else{
						if(Integer.parseInt(mat.group()) <= pVal){

							return true;
						}
					}
				}

			}
		}


//		for(int x = 0;x<)

		return false;
	}

	public int getBlock() {
		// TODO Auto-generated method stub
		return (int)this.cBlock;
	}

	public boolean isABelt() {
		if ( iType.equals("belt") ) {
			System.out.println(iItemName);
			return true;
		}
		else {
			return false;
		}
	}

	// public boolean isCursorItem()
	// {
	// short panel = get_panel();
	//	    
	// switch (panel)
	// {
	// case 0: // equipped or on belt -> not cursor
	// case D2Character.BODY_INV_CONTENT: // inventory
	// case D2Character.BODY_CUBE_CONTENT: // cube
	// case D2Character.BODY_STASH_CONTENT: // stash
	// return false;
	// }
	//	    
	//	    
	// return false;
	// }

}