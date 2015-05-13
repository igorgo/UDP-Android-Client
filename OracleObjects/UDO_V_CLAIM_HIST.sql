--------------------------------------------------------
--  File created - среда-мая-13-2015   
--------------------------------------------------------
--------------------------------------------------------
--  DDL for View UDO_V_CLAIM_HIST
--------------------------------------------------------

  CREATE OR REPLACE FORCE VIEW "UDO_V_CLAIM_HIST" ("NRN", "NPRN", "NCOMPANY", "NCRN", "DCHANGE_DATE", "SAUTHNAME", "SACTION_CODE", "SEVENT_TYPE_NAME", "SSEND_AGN", "SSEND_DIV", "SSEND", "STEXT") AS 
  SELECT M.RN NRN,
          M.PRN NPRN,
          M.COMPANY NCOMPANY,
          M.CRN NCRN,
          M.CHANGE_DATE DCHANGE_DATE,
          UL.NAME SAUTHNAME,
          AUF.CODE SACTION_CODE,
          TS.EVNSTAT_NAME SEVENT_TYPE_NAME,
          AG.AGNABBR SSEND_AGN,
          ID.CODE SSEND_DIV,
          CASE WHEN M.SEND_PERSON IS NULL THEN ID.CODE ELSE AG.AGNABBR END
             SSEND,
          CASE
             WHEN AUF.CODE IN ('CLNEVENTS_INSERT', 'CLNEVENTS_UPDATE')
             THEN
                M.EVENT_DESCR
             WHEN AUF.CODE = 'CLNEVNOTES_INSERT'
             THEN
                ENH.NOTE
             ELSE
                NULL
          END
             STEXT
     FROM CLNEVNHIST M,
          USERLIST UL,
          V_UNITFUNC_SHADOW AUF,
          CLNEVNSTATS TS,
          CLNEVNTYPSTS ES,
          AGNLIST AG,
          CLNPERSONS SP,
          INS_DEPARTMENT ID,
          CLNEVNOTES EN,
          CLNEVNOTESHIST ENH
    WHERE     M.AUTHID = UL.AUTHID
          AND M.ACTION_CODE = AUF.CODE
          AND M.EVENT_STAT = ES.RN
          AND ES.EVENT_STATUS = TS.RN
          AND M.SEND_PERSON = SP.RN(+)
          AND SP.PERS_AGENT = AG.RN(+)
          AND M.SEND_DIVISION = ID.RN(+)
          AND M.NOTE = EN.RN(+)
          AND EN.RN = ENH.PRN(+)
          AND EN.CHANGE_DATE = ENH.CHANGE_DATE(+)
          AND EXISTS
                 (SELECT *
                    FROM V_USERPRIV UP
                   WHERE UP.CATALOG = M.CRN)
