--------------------------------------------------------
--  File created - среда-мая-13-2015   
--------------------------------------------------------
--------------------------------------------------------
--  DDL for View UDO_V_CLAIMS_FILELINKS
--------------------------------------------------------

  CREATE OR REPLACE FORCE VIEW "UDO_V_CLAIMS_FILELINKS" ("NRN", "NCOMPANY", "NCRN", "NPRN", "NLINKRN", "SCATALOG", "SCODE", "SNOTE", "SFILE_PATH", "DLOAD_DATE", "NFILE_TYPE", "SFILE_TYPE", "SFILE_TYPE_NAME", "NCONTENT_TYPE", "SCONTENT_TYPE", "NALLOCATION_TYPE", "SALLOCATION_TYPE", "NMAX_SIZE", "SDEFAULT_FOLDER", "BDATA", "SMIMETYPE", "NSIZE") AS 
  select FL.RN               NRN,
       FL.COMPANY          NCOMPANY,
       FL.CRN              NCRN,
       FUL.TABLE_PRN       NPRN,
       FUL.RN              NLINKRN,
       AC.NAME             SCATALOG,
       FL.CODE             SCODE,
       FL.NOTE             SNOTE,
       FL.FILE_PATH        SFILE_PATH,
       FL.LOAD_DATE        DLOAD_DATE,
       FL.FILE_TYPE        NFILE_TYPE,
       FLT.CODE            SFILE_TYPE,
       FLT.NAME            SFILE_TYPE_NAME,
       FLT.CONTENT_TYPE    NCONTENT_TYPE,
       CT.SCONTENT_TYPE    SCONTENT_TYPE,
       FLT.ALLOCATION_TYPE NALLOCATION_TYPE,
       AT.SALLOCATION_TYPE SALLOCATION_TYPE,
       FLT.MAX_SIZE        NMAX_SIZE,
       FLT.DEFAULT_FOLDER  SDEFAULT_FOLDER,
		 FL.BDATA            BDATA,
		 'application/octet' SMIMETYPE,
		 DBMS_LOB.GETLENGTH(FL.BDATA) NSIZE
  from FILELINKS                    FL,
       FLINKTYPES                   FLT,
       V_FLINKTYPES_CONTENT_BASE    CT,
       V_FLINKTYPES_ALLOCATION_BASE AT,
       ACATALOG                     AC,
       FILELINKSUNITS               FUL
 where FL.FILE_TYPE = FLT.RN
   and FLT.CONTENT_TYPE = CT.NCONTENT_TYPE
   and FLT.ALLOCATION_TYPE = AT.NALLOCATION_TYPE
   and AC.RN = FL.CRN
   and FUL.FILELINKS_PRN = FL.RN
   and FUL.UNITCODE = 'ClientEvents'
   and exists (select *
          from V_USERPRIV UP
         where UP.CATALOG = FL.CRN)
