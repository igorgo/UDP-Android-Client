CREATE OR REPLACE package UDO_PKG_MOBILE_IFACE is

  -- Author  : IGOR-GO
  -- Created : 21.04.2015 11:13:18
  -- Purpose : Мобильный интерфейс УДП

  type T_MOB_REP_REC is record(
    S01 PKG_STD.TSTRING,
    S02 PKG_STD.TSTRING,
    S03 PKG_STD.TSTRING,
    S04 PKG_STD.TSTRING,
    S05 PKG_STD.TSTRING,
    S06 PKG_STD.TSTRING,
    S07 PKG_STD.TSTRING,
    S08 PKG_STD.TSTRING,
    S09 PKG_STD.TSTRING,
    S10 PKG_STD.TSTRING,
    S11 PKG_STD.TSTRING,
    S12 PKG_STD.TSTRING,
    S13 PKG_STD.TSTRING,
    S14 PKG_STD.TSTRING,
    S15 PKG_STD.TSTRING,
    S16 PKG_STD.TSTRING,
    S17 PKG_STD.TSTRING,
    S18 PKG_STD.TSTRING,
    S19 PKG_STD.TSTRING,
    S20 PKG_STD.TSTRING,
    N01 number,
    N02 number,
    N03 number,
    N04 number,
    N05 number,
    N06 number,
    N07 number,
    N08 number,
    N09 number,
    N10 number,
    N11 number,
    N12 number,
    N13 number,
    N14 number,
    N15 number,
    N16 number,
    N17 number,
    N18 number,
    N19 number,
    N20 number,
    D01 date,
    D02 date,
    D03 date,
    D04 date,
    D05 date);

  --
  -- таблица для данных в моб. интерфейсе
  --
  type T_MOB_REP is table of T_MOB_REP_REC;

  type T_MOB_DOC_REC is record(
    MT PKG_STD.TSTRING,
    CT blob);
  type T_MOB_DOC is table of T_MOB_DOC_REC;

  procedure CLAIM_GET_LIST;

  procedure LOGIN
  (
    P_USER   in varchar,
    P_PASS   in varchar,
    P_SESSID out varchar,
    P_PP     out number,
    P_ERROR  out varchar2
  );

  function MYCLAIMS(P_SESSION in varchar2) return T_MOB_REP
    pipelined;

  function APPLISTS return T_MOB_REP
    pipelined;

  function RELEASES return T_MOB_REP
    pipelined;

  function BUILDS(P_VERSION in number) return T_MOB_REP
    pipelined;

  function UNITS return T_MOB_REP
    pipelined;

  procedure STORE_FILTER
  (
    P_SESSION       in varchar2,
    P_FILTER_RN     in number,
    P_FILTER_NAME   in varchar2,
    P_CLAIM_NUMB    in varchar2,
    P_CLAIM_VERS    in varchar2,
    P_CLAIM_RELEASE in varchar2,
    P_CLAIM_BUILD   in varchar2,
    P_CLAIM_UNIT    in varchar2,
    P_CLAIM_APP     in varchar2,
    P_CLAIM_IM_INIT in number,
    P_CLAIM_IM_PERF in number,
    P_CLAIM_CONTENT in varchar2,
    P_OUT_RN        out number,
    P_ERROR         out varchar2
  );

  procedure GET_FILTER
  (
    P_SESSION       in varchar2,
    P_RN            in number,
    P_FILTER_NAME   out varchar2,
    P_CLAIM_NUMB    out varchar2,
    P_CLAIM_VERS    out varchar2,
    P_CLAIM_RELEASE out varchar2,
    P_CLAIM_BUILD   out varchar2,
    P_CLAIM_UNIT    out varchar2,
    P_CLAIM_APP     out varchar2,
    P_CLAIM_IM_INIT out number,
    P_CLAIM_IM_PERF out number,
    P_CLAIM_CONTENT out varchar2,
    P_ERROR         out varchar2
  );

  function CLAIM_BY_COND_RN
  (
    P_SESSION    in varchar2,
    P_COND_RN    in number,
    P_NEW_REC_RN in number
  ) return T_MOB_REP
    pipelined;

  function CLAIM_BY_RN
  (
    P_SESSION in varchar,
    P_RN      in number
  ) return T_MOB_REP
    pipelined;

  function FILTERS(P_SESSION in varchar2) return T_MOB_REP
    pipelined;

  procedure DELETE_FILTER
  (
    P_SESSION   in varchar2,
    P_FILTER_RN in number,
    P_ERROR     out varchar2
  );

  function CLAIM_HISTORY
  (
    P_SESSION in varchar2,
    P_RN      in number
  ) return T_MOB_REP
    pipelined;

  function CLAIM_DOCUMS
  (
    P_SESSION in varchar2,
    P_PRN     in number
  ) return T_MOB_REP
    pipelined;

  -- http://www.freeformatter.com/mime-types-list.html
  -- http://en.wikipedia.org/wiki/Internet_media_type
  -- select distinct lower(substr(file_path,instr(file_path,'.',-1))) from filelinks order by 1;

  function CLAIM_DOCUM
  (
    P_SESSION in varchar2,
    P_RN      in number
  ) return T_MOB_DOC
    pipelined;

  function UNIT_DEPS(P_UNITNAME in varchar) return T_MOB_REP
    pipelined;

  procedure CLAIM_INSERT
  (
    P_SESSION       in varchar2,
    P_TYPE          in varchar2,
    P_PRIORITY      in number,
    P_APP           in varchar2,
    P_UNIT          in varchar2,
    P_FUNC          in varchar2,
    P_DESCRIPTION   in varchar2,
    P_RELEASE_FOUND in varchar2,
    P_BUILD_FOUND   in varchar2,
    P_RELEASE_FIX   in varchar2,
    P_RN            out number,
    P_ERROR         out varchar2
  );

  procedure CLAIM_UPDATE
  (
    P_SESSION       in varchar2,
    P_RN            in number,
    P_DESCRIPTION   in varchar2,
    P_RELEASE_FOUND in varchar2,
    P_BUILD_FOUND   in varchar2,
    P_RELEASE_FIX   in varchar2,
    P_BUILD_FIX     in varchar2,
    P_APP           in varchar2,
    P_UNIT          in varchar2,
    P_FUNC          in varchar2,
    P_PRIORITY      in number,
    P_ERROR         out varchar2
  );

  procedure CLAIM_DELETE
  (
    P_SESSION in varchar2,
    P_NRN     in number,
    P_ERROR   out varchar2,
    P_RESULT  out integer
  );

  procedure CLAIM_CLOSE
  (
    P_SESSION in varchar2,
    P_NRN     in number,
    P_ERROR   out varchar2,
    P_RESULT  out integer
  );
  procedure CLAIM_ADD_NOTE
  (
    P_SESSION in varchar2,
    P_RN      in number,
    P_NOTE    in varchar2,
    P_ERROR   out varchar2,
    P_RESULT  out integer
  );

  procedure CLAIM_ADD_DOC
  (
    P_SESSION  in varchar2,
    P_RN       in number,
    P_FILENAME in varchar2,
    P_FILE     in blob,
    P_ERROR    out varchar2,
    P_RESULT   out integer
  );

  function NEXTPOINTS
  (
    P_SESSION in varchar2,
    P_RN      in number
  ) return T_MOB_REP
    pipelined;

  function WHONEXTSEND
  (
    P_SESSION in varchar2,
    P_RN      in number,
    P_POINT   in number
  ) return T_MOB_REP
    pipelined;

  function WHOSEND
  (
    P_SESSION in varchar2,
    P_RN      in number
  ) return T_MOB_REP
    pipelined;

  function RETPOINTMESSAGE
  (
    P_SESSION in varchar2,
    P_RN      in number
  ) return T_MOB_REP
    pipelined;

  procedure CLAIM_FORWARD
  (
    P_SESSION     in varchar2,
    P_RN          in number,
    P_STAT        in varchar2,
    P_PERSON      in varchar2,
    P_NOTE        in varchar2,
    P_PRIORITY    in number,
    P_RELEASE_FIX in varchar2,
    P_BUILD_FIX   in varchar2,
    P_ERROR       out varchar2,
    P_RESULT      out integer
  );

  procedure CLAIM_RETURN
  (
    P_SESSION in varchar2,
    P_RN      in number,
    P_NOTE    in varchar2,
    P_ERROR   out varchar2,
    P_RESULT  out integer
  );

  procedure CLAIM_SEND
  (
    P_SESSION in varchar2,
    P_RN      in number,
    P_PERSON  in varchar2,
    P_NOTE    in varchar2,
    P_ERROR   out varchar2,
    P_RESULT  out integer
  );

end UDO_PKG_MOBILE_IFACE;

/


CREATE OR REPLACE package body UDO_PKG_MOBILE_IFACE is
  INSTALL_STATE     constant varchar2(12) := 'Включ в ИНСТ';
  INSTALL_NOTE_TYPE constant varchar2(11) := 'Инсталлятор';
  DEFAULT_NOTE_TYPE constant varchar2(10) := 'Примечание';
  DATE_FORMATH      constant varchar2(21) := 'DD.MM.YYYY HH24:MI:SS';
  DATE_FORMAT       constant varchar2(21) := 'DD.MM.YYYY';
  MSG_SESS_EXPIRED  constant varchar2(16) := 'Сессия завершена';
  SESS_V_COND_IDENT constant varchar2(15) := 'CONDITION_IDENT';
  SESS_V_DEPT_RN    constant varchar2(9) := 'DEPART_RN';
  SESS_V_PERS_RN    constant varchar2(9) := 'PERSON_RN';
  COND_STORE_GROUP  constant varchar2(12) := 'CLAIM_MOBILE';
  DEFAULT_COND_RN   constant number := 3092921;
  EVENT_TYPE_ADDON  constant number := 4412;
  EVENT_TYPE_REBUKE constant number := 4424;
  EVENT_TYPE_ERROR  constant number := 4440;

  DEBUG_MODE constant boolean := false;

  L_CLAIM     UDO_V_CLAIMS_APEX%rowtype;
  G_EMPTY_REC T_MOB_REP_REC;

  function C_(S varchar2) return varchar2 deterministic is
    --ST PKG_STD.TSTRING;
  begin
    if S is null then
      return null;
    end if;
    begin
      /*ST := UTL_RAW.CAST_TO_VARCHAR2(UTL_ENCODE.BASE64_DECODE(UTL_RAW.CAST_TO_RAW(S)));
      return CONVERT(ST, 'CL8MSWIN1251', 'UTF8');*/
      return UTL_URL.UNESCAPE(replace(S, '+', ' '), 'UTF8');
    exception
      when others then
        return S;
    end;
  end;

  function BRACKET_(S varchar2) return varchar2 deterministic is
  begin
    if S is not null then
      return '(' || S || ')';
    else
      return null;
    end if;
  end;

  function D2C_
  (
    P_DATE      in date,
    P_WITH_TIME in number default 0
  ) return varchar2 is
  begin
    if P_WITH_TIME = 1 then
      return TO_CHAR(P_DATE, DATE_FORMATH);
    else
      return TO_CHAR(P_DATE, DATE_FORMAT);
    end if;
  end;

  function C2D_(P_DATE in varchar2) return date is
  begin
    return TO_DATE(P_DATE, DATE_FORMATH);
  end;

  procedure LOGIN
  (
    P_USER   in varchar,
    P_PASS   in varchar,
    P_SESSID out varchar,
    P_PP     out number,
    P_ERROR  out varchar2
  ) is
    L_SESS_ID          varchar2(28);
    L_PMO_PERFORMER    PKG_STD.TSTRING;
    L_APP_USER_PESRRN  PKG_STD.TREF;
    L_APP_DEPRN        PKG_STD.TREF;
    L_APP_USERFULLNAME PKG_STD.TSTRING;
    L_APP_DEPCODE      PKG_STD.TSTRING;
  begin
    L_SESS_ID := DBMS_RANDOM.STRING('U', 28);
    begin
      PKG_SESSION.LOGON_WEB(SCONNECT        => L_SESS_ID,
                            SUTILIZER       => C_(P_USER),
                            SPASSWORD       => C_(P_PASS),
                            SIMPLEMENTATION => 'Client',
                            SAPPLICATION    => 'Client',
                            SCOMPANY        => 'ORG',
                            SLANGUAGE       => 'RUSSIAN');
      PKG_SESSION.TIMEOUT_WEB(SCONNECT => L_SESS_ID, NTIMEOUT => 300);
      L_PMO_PERFORMER := UDO_PKG_CLAIMS.GET_SELF_PMO_PERFORMER;
      if L_PMO_PERFORMER is not null then
        P_PP := 1;
      else
        P_PP := 0;
      end if;
      UDO_PKG_CLAIMS.GET_USERPERFORM(L_APP_USER_PESRRN,
                                     L_APP_USERFULLNAME,
                                     L_APP_DEPRN,
                                     L_APP_DEPCODE);
      PKG_SESSION_VARS.PUT(SESS_V_PERS_RN, L_APP_USER_PESRRN);
      PKG_SESSION_VARS.PUT(SESS_V_DEPT_RN, L_APP_DEPRN);
      PKG_SESSION_VARS.PUT(SESS_V_COND_IDENT, GEN_IDENT);
    exception
      when others then
        P_ERROR   := sqlerrm;
        P_PP      := null;
        L_SESS_ID := null;
    end;
    P_SESSID := L_SESS_ID;
  end;

  function SET_SESSION(P_SESSION in varchar2) return boolean is
  
  begin
    begin
      PKG_SESSION.VALIDATE_WEB(P_SESSION);
      return true;
    exception
      when others then
        return false;
    end;
  end;

  function UNIT_DEPS(P_UNITNAME in varchar) return T_MOB_REP
    pipelined is
    cursor LC_APPS(A_UNITNAME varchar2) is
      select distinct ANAME
        from MV_UNITFUNK
       where UNAME = A_UNITNAME
       order by ANAME;
    cursor LC_FUNC(A_UNITNAME varchar2) is
      select distinct FNAME,
                      FNUMB
        from MV_UNITFUNK
       where UNAME = A_UNITNAME
       order by FNUMB;
    L_APP      LC_APPS %rowtype;
    L_FUNC     LC_FUNC %rowtype;
    L_REC      T_MOB_REP_REC;
    L_UNITNAME PKG_STD.TSTRING;
  begin
    L_UNITNAME := C_(P_UNITNAME);
    open LC_APPS(L_UNITNAME);
    loop
      fetch LC_APPS
        into L_APP;
      exit when LC_APPS%notfound;
      L_REC.S01 := 'A';
      L_REC.S02 := L_APP.ANAME;
      pipe row(L_REC);
    end loop;
    close LC_APPS;
  
    open LC_FUNC(L_UNITNAME);
    loop
      fetch LC_FUNC
        into L_FUNC;
      exit when LC_FUNC%notfound;
      L_REC.S01 := 'F';
      L_REC.S02 := L_FUNC.FNAME;
      pipe row(L_REC);
    end loop;
    close LC_FUNC;
  end;

  function APPLISTS return T_MOB_REP
    pipelined is
    cursor LC_APPS is
      select distinct ANAME
        from MV_UNITFUNK
       order by ANAME;
    L_APP LC_APPS %rowtype;
    L_REC T_MOB_REP_REC;
  begin
    open LC_APPS;
    loop
      fetch LC_APPS
        into L_APP;
      exit when LC_APPS%notfound;
      L_REC.S01 := L_APP.ANAME;
      pipe row(L_REC);
    end loop;
    close LC_APPS;
  end;

  function UNITS return T_MOB_REP
    pipelined is
    cursor LC_UNITS is
      select distinct UNAME
        from MV_UNITFUNK
       order by UNAME;
    L_UNIT LC_UNITS %rowtype;
    L_REC  T_MOB_REP_REC;
  begin
    open LC_UNITS;
    loop
      fetch LC_UNITS
        into L_UNIT;
      exit when LC_UNITS%notfound;
      L_REC.S01 := L_UNIT.UNAME;
      pipe row(L_REC);
    end loop;
    close LC_UNITS;
  end;

  function RELEASES return T_MOB_REP
    pipelined is
    cursor LC_RELS is
      select *
        from UDO_SOFTRELEASES;
    L_REL LC_RELS %rowtype;
    L_REC T_MOB_REP_REC;
  begin
    open LC_RELS;
    loop
      fetch LC_RELS
        into L_REL;
      exit when LC_RELS%notfound;
      L_REC.N01 := L_REL.RN;
      L_REC.S01 := L_REL.SOFTVERSION;
      L_REC.S02 := L_REL.RELNAME;
      pipe row(L_REC);
    end loop;
    close LC_RELS;
  end;

  function BUILDS(P_VERSION in number) return T_MOB_REP
    pipelined is
    cursor LC_BLDS is
      select *
        from UDO_SOFTBUILDS
       where PRN = P_VERSION;
    L_BLD LC_BLDS %rowtype;
    L_REC T_MOB_REP_REC;
  begin
    open LC_BLDS;
    loop
      fetch LC_BLDS
        into L_BLD;
      exit when LC_BLDS%notfound;
      L_REC.N01 := L_BLD.RN;
      L_REC.N02 := L_BLD.PRN;
      L_REC.S01 := L_BLD.CODE;
      L_REC.S02 := trim(D2C_(L_BLD.BUILDATE));
      pipe row(L_REC);
    end loop;
    close LC_BLDS;
  end;

  /*
  select T.SPERSON as V,
         T.SPERSON as L
    from V_EVRTPTEXEC_BY_PERSON T
   where (T.NEVENT = :P8_NRN or T.NEVENT is null)
     and T.NPOINT in (select P.NRN
                        from V_EVRTPOINTS P
                       where P.NCOMPANY = :APP_COMPANY
                         and P.SEVENT_TYPE = :P8_SEVENT_TYPE
                         and P.SEVENT_STATUS = :P8_CS_SEVENT_STAT)
     and exists (select null
            from V_EVRTPTPASS R
           where R.NCOMPANY = :APP_COMPANY
             and R.SEVENT_TYPE = :P8_SEVENT_TYPE
             and R.SNEXT_POINT = :P8_CS_SEVENT_STAT
             and R.NDEFAULT_EXEC = 0)
   order by T.SPERSON
  */

  function RETPOINTMESSAGE
  (
    P_SESSION in varchar2,
    P_RN      in number
  ) return T_MOB_REP
    pipelined is
    L_MESS varchar2(4000);
    L_REC  T_MOB_REP_REC;
  begin
    if not SET_SESSION(P_SESSION) then
      return;
    end if;
    FIND_CLNEVENTS_RETPOINT(NCOMPANY   => PKG_SESSION.GET_COMPANY,
                            NRN        => P_RN,
                            NPOINT_OUT => PKG_STD.VREF,
                            SCOMMENTRY => L_MESS);
    L_REC     := G_EMPTY_REC;
    L_REC.S01 := L_MESS;
    pipe row(L_REC);
  end;

  function WHOSEND
  (
    P_SESSION in varchar2,
    P_RN      in number
  ) return T_MOB_REP
    pipelined is
    cursor LC_EXECS is
      select T.PERSON_CODE,
             A.AGNABBR,
             T.POST_CODE,
             T.DIVISION_CODE
        from V_EVRTPTEXEC_PERSON T,
             CLNEVENTS           E,
             EVRTPOINTS          P,
             CLNPERSONS          PRS,
             AGNLIST             A
       where E.RN = P_RN
         and T.EVENT is null
         and P.EVENT_STATUS = E.EVENT_STAT
         and T.POINT = P.RN
         and (T.PERSON != E.SEND_PERSON or E.SEND_PERSON is null)
         and T.PERSON = PRS.RN
         and PRS.PERS_AGENT = A.RN
       order by T.PERSON_CODE;
    L_EXEC LC_EXECS %rowtype;
    L_REC  T_MOB_REP_REC;
  begin
    if not SET_SESSION(C_(P_SESSION)) then
      return;
    end if;
    open LC_EXECS;
  
    loop
      fetch LC_EXECS
        into L_EXEC;
      exit when LC_EXECS%notfound;
      L_REC     := G_EMPTY_REC;
      L_REC.S01 := L_EXEC.PERSON_CODE;
      L_REC.S02 := STRCOMBINE(L_EXEC.AGNABBR,
                              BRACKET_(STRCOMBINE(L_EXEC.POST_CODE,
                                                  L_EXEC.DIVISION_CODE)),
                              ' ');
      pipe row(L_REC);
    end loop;
    close LC_EXECS;
  end;

  function WHONEXTSEND
  (
    P_SESSION in varchar2,
    P_RN      in number,
    P_POINT   in number
  ) return T_MOB_REP
    pipelined is
  
    cursor LC_DEFEXEC is
      select count(*) CNT
        from DUAL
       where exists (select null
                from EVRTPTPASS R,
                     EVRTPOINTS P,
                     CLNEVENTS  E
               where E.RN = P_RN
                 and P.EVENT_STATUS = E.EVENT_STAT
                 and R.PRN = P.RN
                 and R.NEXT_POINT = P_POINT
                 and R.DEFAULT_EXEC = 0);
  
    cursor LC_EXECS is
      select T.PERSON_CODE,
             A.AGNABBR,
             T.POST_CODE,
             T.DIVISION_CODE
        from V_EVRTPTEXEC_PERSON T,
             CLNPERSONS          PRS,
             AGNLIST             A
       where T.EVENT is null
         and T.POINT = P_POINT
         and T.PERSON = PRS.RN
         and PRS.PERS_AGENT = A.RN
       order by T.PERSON_CODE;
    L_EXEC LC_EXECS %rowtype;
    L_REC  T_MOB_REP_REC;
    L_CNT  number;
  begin
    if SET_SESSION(P_SESSION) then
      open LC_DEFEXEC;
      fetch LC_DEFEXEC
        into L_CNT;
      close LC_DEFEXEC;
    
      if L_CNT = 0 then
        L_REC     := G_EMPTY_REC;
        L_REC.S01 := null;
        L_REC.S02 := 'Исполнитель по умолчанию';
        pipe row(L_REC);
      else
      
        open LC_EXECS;
        loop
          fetch LC_EXECS
            into L_EXEC;
          exit when LC_EXECS%notfound;
          L_REC     := G_EMPTY_REC;
          L_REC.S01 := L_EXEC.PERSON_CODE;
          L_REC.S02 := STRCOMBINE(L_EXEC.AGNABBR,
                                  BRACKET_(STRCOMBINE(L_EXEC.POST_CODE,
                                                      L_EXEC.DIVISION_CODE)),
                                  ' ');
          pipe row(L_REC);
        end loop;
        close LC_EXECS;
      end if;
    end if;
  end;

  function NEXTPOINTS
  (
    P_SESSION in varchar2,
    P_RN      in number
  ) return T_MOB_REP
    pipelined is
  
    cursor LC_POINTS is
      select P.RN as POINT,
             S.EVNSTAT_CODE
        from EVRTPOINTS   M,
             EVROUTES     R,
             CLNEVENTS    E,
             EVRTPTPASS   T,
             EVRTPOINTS   P,
             CLNEVNTYPSTS NS,
             CLNEVNSTATS  S
       where E.RN = P_RN
         and R.EVENT_TYPE = E.EVENT_TYPE
         and M.EVENT_STATUS = E.EVENT_STAT
         and M.PRN = R.RN
         and T.PRN = M.RN
         and T.NEXT_POINT = P.RN
         and P.EVENT_STATUS = NS.RN
         and NS.EVENT_STATUS = S.RN
       order by S.EVNSTAT_CODE;
    L_POINT LC_POINTS %rowtype;
    L_REC   T_MOB_REP_REC;
  begin
    if SET_SESSION(P_SESSION) then
      open LC_POINTS;
      loop
        fetch LC_POINTS
          into L_POINT;
        exit when LC_POINTS%notfound;
        L_REC     := G_EMPTY_REC;
        L_REC.S01 := L_POINT.EVNSTAT_CODE;
        L_REC.N01 := L_POINT.POINT;
        pipe row(L_REC);
      end loop;
      close LC_POINTS;
    end if;
  end;

  procedure CLAIM_UPDATE
  (
    P_SESSION       in varchar2,
    P_RN            in number,
    P_DESCRIPTION   in varchar2,
    P_RELEASE_FOUND in varchar2,
    P_BUILD_FOUND   in varchar2,
    P_RELEASE_FIX   in varchar2,
    P_BUILD_FIX     in varchar2,
    P_APP           in varchar2,
    P_UNIT          in varchar2,
    P_FUNC          in varchar2,
    P_PRIORITY      in number,
    P_ERROR         out varchar2
  ) is
    cursor L_EVN is
      select T.PRIORITY
        from CLNEVENTS T
       where T.RN = P_RN;
    L_PRIORITY number;
  begin
    if not SET_SESSION(P_SESSION) then
      P_ERROR := MSG_SESS_EXPIRED;
      return;
    end if;
    begin
      UDO_PKG_CLAIMS.CLAIM_UPDATE(NRN           => P_RN,
                                  SLINKED_CLAIM => null,
                                  SEVENT_DESCR  => C_(P_DESCRIPTION),
                                  SREL_FROM     => C_(P_RELEASE_FOUND),
                                  SBUILD_FROM   => C_(P_BUILD_FOUND),
                                  SREL_TO       => C_(P_RELEASE_FIX),
                                  SBUILD_TO     => C_(P_BUILD_FIX),
                                  SMODULE       => C_(P_APP),
                                  SUNITCODE     => C_(P_UNIT),
                                  SUNITFUNC     => C_(P_FUNC));
    
      open L_EVN;
      fetch L_EVN
        into L_PRIORITY;
      close L_EVN;
    
      if ((P_PRIORITY is not null) and (L_PRIORITY != P_PRIORITY)) then
        P_CLNEVENTS_SET_PRIORITY(NCOMPANY  => PKG_SESSION.GET_COMPANY,
                                 NRN       => P_RN,
                                 NPRIORITY => P_PRIORITY);
      end if;
    exception
      when others then
        P_ERROR := sqlerrm;
    end;
  
  end;

  procedure CLAIM_INSERT
  (
    P_SESSION       in varchar2,
    P_TYPE          in varchar2,
    P_PRIORITY      in number,
    P_APP           in varchar2,
    P_UNIT          in varchar2,
    P_FUNC          in varchar2,
    P_DESCRIPTION   in varchar2,
    P_RELEASE_FOUND in varchar2,
    P_BUILD_FOUND   in varchar2,
    P_RELEASE_FIX   in varchar2,
    P_RN            out number,
    P_ERROR         out varchar2
  ) is
    /* L_TYPE CLNEVNTYPES.EVNTYPE_CODE%type;*/
  begin
    if not SET_SESSION(P_SESSION) then
      P_ERROR := MSG_SESS_EXPIRED;
      return;
    end if;
  
    /*    case P_TYPE
      when 1 then
        L_TYPE := 'ДОРАБОТКА';
      when 2 then
        L_TYPE := 'ЗАМЕЧАНИЕ';
      when 3 then
        L_TYPE := 'ОШИБКА';
    end case;*/
    begin
      UDO_PKG_CLAIMS.CLAIM_INSERT(NCRN                => null,
                                  SEVENT_TYPE         => C_(P_TYPE),
                                  SLINKED_CLAIM       => null,
                                  NPRIORITY           => P_PRIORITY,
                                  NSEND_TO_DEVELOPERS => 0,
                                  SINIT_PERSON        => null,
                                  SMODULE             => C_(P_APP),
                                  SUNITCODE           => C_(P_UNIT),
                                  SUNITFUNC           => C_(P_FUNC),
                                  SEVENT_DESCR        => SUBSTR(C_(P_DESCRIPTION),
                                                                1,
                                                                4000),
                                  SREL_FROM           => C_(P_RELEASE_FOUND),
                                  SBUILD_FROM         => C_(P_BUILD_FOUND),
                                  SREL_TO             => C_(P_RELEASE_FIX),
                                  NRN                 => P_RN);
    exception
      when others then
        P_ERROR := sqlerrm;
        P_RN    := null;
    end;
  end;

  procedure CLAIM_DELETE
  (
    P_SESSION in varchar2,
    P_NRN     in number,
    P_ERROR   out varchar2,
    P_RESULT  out integer
  ) is
  begin
    if not SET_SESSION(C_(P_SESSION)) then
      P_ERROR  := MSG_SESS_EXPIRED;
      P_RESULT := -1;
      return;
    end if;
    begin
      UDO_PKG_CLAIMS.CLAIM_DELETE(NRN => P_NRN);
      P_RESULT := 0;
    exception
      when others then
        P_ERROR  := sqlerrm;
        P_RESULT := sqlcode;
    end;
  end;

  procedure CLAIM_ADD_DOC
  (
    P_SESSION  in varchar2,
    P_RN       in number,
    P_FILENAME in varchar2,
    P_FILE     in blob,
    P_ERROR    out varchar2,
    P_RESULT   out integer
  ) is
  
  begin
    /*      if P_FILE is null then
            P_ERROR := 'пустой blob файла ' || P_FILENAME;
          else
            P_ERROR := 'размер '|| P_FILENAME || ':' || DBMS_LOB.getlength(P_FILE);
          end if;  
          return;
    */
    if not SET_SESSION(C_(P_SESSION)) then
      P_ERROR  := MSG_SESS_EXPIRED;
      P_RESULT := -1;
      return;
    end if;
    begin
      UDO_PKG_CLAIMS.CLAIM_ADD_LINKDOC(NCLAIM_RN     => P_RN,
                                       BTEMPLATE     => P_FILE,
                                       SLINKDOC_TYPE => null,
                                       SLINKDOC_PATH => C_(P_FILENAME));
      P_RESULT := 0;
    exception
      when others then
        P_RESULT := sqlcode;
        P_ERROR  := sqlerrm;
    end;
  end;

  procedure CLAIM_RETURN
  (
    P_SESSION in varchar2,
    P_RN      in number,
    P_NOTE    in varchar2,
    P_ERROR   out varchar2,
    P_RESULT  out integer
  ) is
  begin
    if not SET_SESSION(C_(P_SESSION)) then
      P_ERROR  := MSG_SESS_EXPIRED;
      P_RESULT := -1;
      return;
    end if;
    begin
      UDO_PKG_CLAIMS.CLAIM_RETURN(NRN          => P_RN,
                                  SNOTE_HEADER => DEFAULT_NOTE_TYPE,
                                  SNOTE        => C_(P_NOTE));
      P_RESULT := 0;
    exception
      when others then
        P_RESULT := sqlcode;
        P_ERROR  := sqlerrm;
    end;
  end;

  procedure CLAIM_SEND
  (
    P_SESSION in varchar2,
    P_RN      in number,
    P_PERSON  in varchar2,
    P_NOTE    in varchar2,
    P_ERROR   out varchar2,
    P_RESULT  out integer
  ) is
    cursor LC_CLAIM is
      select TT.EVNTYPE_CODE,
             S.EVNSTAT_CODE
        from CLNEVENTS    T,
             CLNEVNTYPES  TT,
             CLNEVNTYPSTS ES,
             CLNEVNSTATS  S
       where T.RN = P_RN
         and T.EVENT_TYPE = TT.RN
         and T.EVENT_STAT = ES.RN
         and ES.EVENT_STATUS = S.RN;
    L_CLAIM LC_CLAIM%rowtype;
  begin
    if not SET_SESSION(C_(P_SESSION)) then
      P_ERROR  := MSG_SESS_EXPIRED;
      P_RESULT := -1;
      return;
    end if;
    begin
      open LC_CLAIM;
      fetch LC_CLAIM
        into L_CLAIM;
      close LC_CLAIM;
    
      UDO_PKG_CLAIMS.CLAIM_DO_SEND(NRN            => P_RN,
                                   SEVENT_TYPE    => L_CLAIM.EVNTYPE_CODE,
                                   SEVENT_STAT    => L_CLAIM.EVNSTAT_CODE,
                                   SSEND_CLIENT   => null,
                                   SSEND_DIVISION => null,
                                   SSEND_POST     => null,
                                   SSEND_PERFORM  => null,
                                   SSEND_PERSON   => C_(P_PERSON),
                                   SNOTE_HEADER   => DEFAULT_NOTE_TYPE,
                                   SNOTE          => C_(P_NOTE));
      P_RESULT := 0;
    exception
      when others then
        P_RESULT := sqlcode;
        P_ERROR  := sqlerrm;
    end;
  end;

  procedure CLAIM_FORWARD
  (
    P_SESSION     in varchar2,
    P_RN          in number,
    P_STAT        in varchar2,
    P_PERSON      in varchar2,
    P_NOTE        in varchar2,
    P_PRIORITY    in number,
    P_RELEASE_FIX in varchar2,
    P_BUILD_FIX   in varchar2,
    P_ERROR       out varchar2,
    P_RESULT      out integer
  ) is
    cursor LC_CLAIM is
      select TT.EVNTYPE_CODE
        from CLNEVENTS   T,
             CLNEVNTYPES TT
       where T.RN = P_RN
         and T.EVENT_TYPE = TT.RN;
  
    L_CLAIM     LC_CLAIM%rowtype;
    L_NOTE_TYPE CLNEVNTNOTETYPES.CODE%type;
  begin
    if not SET_SESSION(C_(P_SESSION)) then
      P_ERROR  := MSG_SESS_EXPIRED;
      P_RESULT := -1;
      return;
    end if;
    begin
      open LC_CLAIM;
      fetch LC_CLAIM
        into L_CLAIM;
      close LC_CLAIM;
    
      if P_STAT = INSTALL_STATE then
        L_NOTE_TYPE := DEFAULT_NOTE_TYPE;
      else
        L_NOTE_TYPE := INSTALL_NOTE_TYPE;
      end if;
    
      UDO_PKG_CLAIMS.CLAIM_CHANGE_STATE(NRN            => P_RN,
                                        SEVENT_TYPE    => L_CLAIM.EVNTYPE_CODE,
                                        SEVENT_STAT    => C_(P_STAT),
                                        SSEND_CLIENT   => null,
                                        SSEND_DIVISION => null,
                                        SSEND_POST     => null,
                                        SSEND_PERFORM  => null,
                                        SSEND_PERSON   => C_(P_PERSON),
                                        SNOTE_HEADER   => L_NOTE_TYPE,
                                        SNOTE          => C_(P_NOTE),
                                        NPRIORITY      => P_PRIORITY,
                                        SREL_TO        => C_(P_RELEASE_FIX),
                                        SBUILD_TO      => C_(P_BUILD_FIX));
      P_RESULT := 0;
    exception
      when others then
        P_RESULT := sqlcode;
        P_ERROR  := sqlerrm;
    end;
  end;

  procedure CLAIM_ADD_NOTE
  (
    P_SESSION in varchar2,
    P_RN      in number,
    P_NOTE    in varchar2,
    P_ERROR   out varchar2,
    P_RESULT  out integer
  ) is
  
  begin
    if not SET_SESSION(C_(P_SESSION)) then
      P_ERROR  := MSG_SESS_EXPIRED;
      P_RESULT := -1;
      return;
    end if;
    begin
      P_CLNEVNOTES_INSERT(NCOMPANY     => PKG_SESSION.GET_COMPANY,
                          NPRN         => P_RN,
                          SCLIENT      => null,
                          SAUTHID      => null,
                          SNOTE_HEADER => DEFAULT_NOTE_TYPE,
                          SNOTE        => C_(P_NOTE),
                          NRN          => PKG_STD.VREF);
      P_RESULT := 0;
    exception
      when others then
        P_RESULT := sqlcode;
        P_ERROR  := sqlerrm;
    end;
  end;

  procedure CLAIM_CLOSE
  (
    P_SESSION in varchar2,
    P_NRN     in number,
    P_ERROR   out varchar2,
    P_RESULT  out integer
  ) is
  begin
    if not SET_SESSION(C_(P_SESSION)) then
      P_ERROR  := MSG_SESS_EXPIRED;
      P_RESULT := -1;
      return;
    end if;
    begin
      UDO_PKG_CLAIMS.CLAIM_CLOSE(NRN => P_NRN);
      P_RESULT := 0;
    exception
      when others then
        P_ERROR  := sqlerrm;
        P_RESULT := sqlcode;
    end;
  end;

  function MYCLAIMS(P_SESSION in varchar2) return T_MOB_REP
    pipelined is
    cursor LC_CLAIMS is
      select SEVENT_TYPE,
             trim(SEVENT_NUMB) SNUMB,
             SUBSTR(SEVENT_DESCR, 1, 255) || '…' SDESC,
             NRN
        from UDO_V_CLAIMS
       where NSEND_PERSON = PKG_SESSION_VARS.GET_NUM(SESS_V_PERS_RN)
         and NCLOSED = 0
       order by DCHANGE_DATE desc;
    L_CLAIM LC_CLAIMS%rowtype;
    L_REC   T_MOB_REP_REC;
  begin
    if SET_SESSION(P_SESSION) then
      open LC_CLAIMS;
      loop
        fetch LC_CLAIMS
          into L_CLAIM;
        exit when LC_CLAIMS%notfound;
        L_REC.S01 := L_CLAIM.SEVENT_TYPE;
        L_REC.S02 := L_CLAIM.SNUMB;
        L_REC.S03 := L_CLAIM.SDESC;
        L_REC.N01 := L_CLAIM.NRN;
        pipe row(L_REC);
      end loop;
      close LC_CLAIMS;
    end if;
  end;

  procedure GET_FILTER
  (
    P_SESSION       in varchar2,
    P_RN            in number,
    P_FILTER_NAME   out varchar2,
    P_CLAIM_NUMB    out varchar2,
    P_CLAIM_VERS    out varchar2,
    P_CLAIM_RELEASE out varchar2,
    P_CLAIM_BUILD   out varchar2,
    P_CLAIM_UNIT    out varchar2,
    P_CLAIM_APP     out varchar2,
    P_CLAIM_IM_INIT out number,
    P_CLAIM_IM_PERF out number,
    P_CLAIM_CONTENT out varchar2,
    P_ERROR         out varchar2
  ) is
    L_RN PKG_STD.TREF;
  begin
    if SET_SESSION(P_SESSION) then
      L_RN := P_RN;
      if L_RN is not null then
        P_FILTER_NAME := UDO_PKG_COND_STORE.FIND_NAME_BY_RN(L_RN);
      else
        L_RN := UDO_PKG_COND_STORE.GET_DAEFAULT_STORE(COND_STORE_GROUP);
      end if;
      if L_RN is not null then
        UDO_PKG_COND_STORE.GET_STORE_ATTR_VAL(L_RN,
                                              'APP_COND_NUMBER',
                                              P_CLAIM_NUMB);
        UDO_PKG_COND_STORE.GET_STORE_ATTR_VAL(L_RN,
                                              'APP_COND_VERSION',
                                              P_CLAIM_VERS);
        UDO_PKG_COND_STORE.GET_STORE_ATTR_VAL(L_RN,
                                              'APP_COND_RELEASE',
                                              P_CLAIM_RELEASE);
        UDO_PKG_COND_STORE.GET_STORE_ATTR_VAL(L_RN,
                                              'APP_COND_BUILD',
                                              P_CLAIM_BUILD);
        UDO_PKG_COND_STORE.GET_STORE_ATTR_VAL(L_RN,
                                              'APP_COND_UNITCODE',
                                              P_CLAIM_UNIT);
        UDO_PKG_COND_STORE.GET_STORE_ATTR_VAL(L_RN,
                                              'APP_COND_APPLICATION',
                                              P_CLAIM_APP);
        UDO_PKG_COND_STORE.GET_STORE_ATTR_VAL(L_RN,
                                              'APP_COND_INIT_IS_ME',
                                              P_CLAIM_IM_INIT);
        UDO_PKG_COND_STORE.GET_STORE_ATTR_VAL(L_RN,
                                              'APP_COND_PERF_IS_ME',
                                              P_CLAIM_IM_PERF);
        UDO_PKG_COND_STORE.GET_STORE_ATTR_VAL(L_RN,
                                              'APP_COND_CONTENT',
                                              P_CLAIM_CONTENT);
      end if;
    else
      P_ERROR := MSG_SESS_EXPIRED;
    end if;
  end;

  procedure STORE_FILTER
  (
    P_SESSION       in varchar2,
    P_FILTER_RN     in number,
    P_FILTER_NAME   in varchar2,
    P_CLAIM_NUMB    in varchar2,
    P_CLAIM_VERS    in varchar2,
    P_CLAIM_RELEASE in varchar2,
    P_CLAIM_BUILD   in varchar2,
    P_CLAIM_UNIT    in varchar2,
    P_CLAIM_APP     in varchar2,
    P_CLAIM_IM_INIT in number,
    P_CLAIM_IM_PERF in number,
    P_CLAIM_CONTENT in varchar2,
    P_OUT_RN        out number,
    P_ERROR         out varchar2
  ) is
  begin
    if SET_SESSION(P_SESSION) then
      UDO_PKG_COND_STORE.PROLOGUE(P_RN         => P_FILTER_RN,
                                  P_COND_GROUP => COND_STORE_GROUP,
                                  P_STORE_NAME => C_(P_FILTER_NAME));
      /*
          UDO_PKG_COND_STORE.ADD_VALUE(P_NAME  => 'APP_USER_PESRRN',
                                         P_VALUE => PKG_SESSION_VARS.GET_NUM(SESS_V_PERS_RN));
          UDO_PKG_COND_STORE.ADD_VALUE(P_NAME  => 'APP_DEPRN',
                                         P_VALUE => PKG_SESSION_VARS.GET_NUM(SESS_V_DEPT_RN));
          APP_COND_CONTENT_IN_NOTE = 1
      */
    
      UDO_PKG_COND_STORE.ADD_VALUE(P_NAME  => 'APP_COND_NUMBER',
                                   P_VALUE => C_(P_CLAIM_NUMB));
      UDO_PKG_COND_STORE.ADD_VALUE(P_NAME  => 'APP_COND_VERSION',
                                   P_VALUE => C_(P_CLAIM_VERS));
      UDO_PKG_COND_STORE.ADD_VALUE(P_NAME  => 'APP_COND_RELEASE',
                                   P_VALUE => C_(P_CLAIM_RELEASE));
      UDO_PKG_COND_STORE.ADD_VALUE(P_NAME  => 'APP_COND_BUILD',
                                   P_VALUE => C_(P_CLAIM_BUILD));
      UDO_PKG_COND_STORE.ADD_VALUE(P_NAME  => 'APP_COND_UNITCODE',
                                   P_VALUE => C_(P_CLAIM_UNIT));
      UDO_PKG_COND_STORE.ADD_VALUE(P_NAME  => 'APP_COND_APPLICATION',
                                   P_VALUE => C_(P_CLAIM_APP));
      UDO_PKG_COND_STORE.ADD_VALUE(P_NAME  => 'APP_COND_INIT_IS_ME',
                                   P_VALUE => P_CLAIM_IM_INIT);
      UDO_PKG_COND_STORE.ADD_VALUE(P_NAME  => 'APP_COND_PERF_IS_ME',
                                   P_VALUE => P_CLAIM_IM_PERF);
      UDO_PKG_COND_STORE.ADD_VALUE(P_NAME  => 'APP_COND_CONTENT',
                                   P_VALUE => C_(P_CLAIM_CONTENT));
    
      UDO_PKG_COND_STORE.EPILOGUE(P_RN => P_OUT_RN);
    else
      P_ERROR := MSG_SESS_EXPIRED;
    end if;
  end;

  procedure SET_CONDS_
  (
    P_IDENT   in number,
    P_COND_RN in number
  ) is
    pragma autonomous_transaction;
    --I         binary_integer;
    L_COND_RN number;
  begin
    if (P_COND_RN is null) or (P_COND_RN = 0) then
      L_COND_RN := DEFAULT_COND_RN;
    else
      L_COND_RN := P_COND_RN;
    end if;
    PKG_COND_BROKER.PROLOGUE(PKG_COND_BROKER.MODE_SMART_, P_IDENT);
    PKG_COND_BROKER.SET_COMPANY(PKG_SESSION.GET_COMPANY);
    UDO_PKG_COND_STORE.SET_COND_TO_BROKER(L_COND_RN);
    PKG_COND_BROKER.SET_CONDITION_NUM('APP_USER_PESRRN',
                                      PKG_SESSION_VARS.GET_NUM(SESS_V_PERS_RN));
    PKG_COND_BROKER.SET_CONDITION_NUM('APP_DEPRN',
                                      PKG_SESSION_VARS.GET_NUM(SESS_V_DEPT_RN));
    PKG_COND_BROKER.SET_PROCEDURE('UDO_P_WEBUDP_BASE_COND');
    PKG_COND_BROKER.EPILOGUE;
  end;

  procedure DELETE_FILTER
  (
    P_SESSION   in varchar2,
    P_FILTER_RN in number,
    P_ERROR     out varchar2
  ) is
  begin
    if SET_SESSION(P_SESSION) then
      UDO_PKG_COND_STORE.DEL(P_FILTER_RN);
    else
      P_ERROR := MSG_SESS_EXPIRED;
    end if;
  end;

  function FILTERS(P_SESSION in varchar2) return T_MOB_REP
    pipelined is
    cursor LC_FILTERS is
      select *
        from table(UDO_PKG_COND_STORE.V(COND_STORE_GROUP)) T
       order by T.EDITABLE,
                STORE_NAME;
    L_FILTER LC_FILTERS%rowtype;
    function FILTER_TO_REC(A_FILTER LC_FILTERS%rowtype) return T_MOB_REP_REC is
      LL_REC T_MOB_REP_REC;
    begin
      LL_REC     := G_EMPTY_REC;
      LL_REC.N01 := A_FILTER.RN;
      LL_REC.S01 := A_FILTER.STORE_NAME;
      LL_REC.S02 := A_FILTER.EDITABLE;
      return LL_REC;
    end;
  begin
    if SET_SESSION(P_SESSION) then
      open LC_FILTERS;
      loop
        fetch LC_FILTERS
          into L_FILTER;
        exit when LC_FILTERS%notfound;
        pipe row(FILTER_TO_REC(L_FILTER));
      end loop;
      close LC_FILTERS;
    end if;
  end;

  function CLAIM_BY_RN
  (
    P_SESSION in varchar,
    P_RN      in number
  ) return T_MOB_REP
    pipelined is
    cursor LC_CLAIM is
      select *
        from UDO_V_CLAIMS_MOBILE_IFACE T
       where T.RN = P_RN;
    L_CLAIM LC_CLAIM%rowtype;
    L_REC   T_MOB_REP_REC;
  begin
    if SET_SESSION(P_SESSION) then
      open LC_CLAIM;
      fetch LC_CLAIM
        into L_CLAIM;
      close LC_CLAIM;
      L_REC     := G_EMPTY_REC;
      L_REC.S01 := L_CLAIM.EVENT_NUMB;
      case L_CLAIM.EVENT_TYPE
        when EVENT_TYPE_ADDON then
          L_REC.N01 := 1;
        when EVENT_TYPE_REBUKE then
          L_REC.N01 := 2;
        when EVENT_TYPE_ERROR then
          L_REC.N01 := 3;
        else
          L_REC.N01 := 0;
      end case;
      L_REC.S02 := L_CLAIM.EVNSTAT_CODE;
      L_REC.N02 := L_CLAIM.STATUDPTYPE;
      L_REC.S03 := D2C_(L_CLAIM.REG_DATE);
      L_REC.S04 := L_CLAIM.INITIATOR;
      L_REC.S05 := D2C_(L_CLAIM.CHANGE_DATE);
      if L_CLAIM.EXECUTOR_DEP is null then
        if L_CLAIM.EXECUTOR != 'Архив' then
          L_REC.S06 := L_CLAIM.EXECUTOR;
          L_REC.N03 := 1;
        else
          L_REC.S06 := '';
          L_REC.N03 := 0;
        end if;
      else
        L_REC.S06 := L_CLAIM.EXECUTOR_DEP;
        L_REC.N03 := 2;
      end if;
      L_REC.N04 := L_CLAIM.REL_FR_RN;
      L_REC.N05 := L_CLAIM.BLD_FR_RN;
      L_REC.N06 := L_CLAIM.REL_TO_RN;
      L_REC.N07 := L_CLAIM.BLD_TO_RN;
      L_REC.N08 := L_CLAIM.PRIORITY;
      L_REC.S07 := L_CLAIM.ACODE;
      L_REC.S08 := L_CLAIM.UNITCODE;
      L_REC.S09 := L_CLAIM.FCODE;
      L_REC.S10 := L_CLAIM.EVENT_DESCR;
      UDO_PKG_CLAIMS.GET_AVAIL_ACTIONS(NRN       => P_RN,
                                       NUPDATE   => L_REC.N09,
                                       NDELETE   => L_REC.N10,
                                       NSTATE    => L_REC.N11,
                                       NSEND     => L_REC.N12,
                                       NRETURN   => L_REC.N13,
                                       NCLOSE    => L_REC.N14,
                                       NADDNOTE  => L_REC.N15,
                                       NADDDOCUM => L_REC.N16);
    
      pipe row(L_REC);
    end if;
  end;

  function CLAIM_BY_COND_RN
  (
    P_SESSION    in varchar2,
    P_COND_RN    in number,
    P_NEW_REC_RN in number
  ) return T_MOB_REP
    pipelined is
    --I       binary_integer;
    L_IDENT number;
    cursor LC_LAST_CLAIM(A_RN number) is
      select *
        from UDO_V_CLAIMS_MOBILE_IFACE T
       where T.RN = A_RN;
    cursor LC_CLAIMS(A_IDENT number) is
      select *
        from UDO_V_CLAIMS_MOBILE_IFACE T
       where T.RN in (select S.ID
                        from COND_BROKER_IDSMART S
                       where S.IDENT = A_IDENT)
       order by T.CHANGE_DATE desc;
  
    L_CLAIM LC_LAST_CLAIM%rowtype;
    function CLAIM_TO_REC(A_CLAIM in LC_LAST_CLAIM%rowtype) return T_MOB_REP_REC is
      LL_REC T_MOB_REP_REC;
    begin
      LL_REC     := G_EMPTY_REC;
      LL_REC.S01 := trim(A_CLAIM.EVENT_NUMB);
      LL_REC.S02 := A_CLAIM.REL_BLD_REL;
      LL_REC.S03 := D2C_(A_CLAIM.REG_DATE);
      LL_REC.S04 := A_CLAIM.UNITCODE;
      LL_REC.S05 := A_CLAIM.ACODE;
      LL_REC.S06 := A_CLAIM.EVNSTAT_CODE;
      LL_REC.S07 := A_CLAIM.INITIATOR;
      LL_REC.S08 := A_CLAIM.EVENT_DESCR;
      if A_CLAIM.EXECUTOR_DEP is null then
        if A_CLAIM.EXECUTOR != 'Архив' then
          LL_REC.S09 := A_CLAIM.EXECUTOR;
          LL_REC.N07 := 1;
        else
          LL_REC.S09 := '';
          LL_REC.N07 := 0;
        end if;
      else
        LL_REC.S09 := A_CLAIM.EXECUTOR_DEP;
        LL_REC.N07 := 2;
      end if;
      LL_REC.S10 := D2C_(A_CLAIM.CHANGE_DATE);
      LL_REC.N01 := A_CLAIM.RN;
      case A_CLAIM.EVENT_TYPE
        when EVENT_TYPE_ADDON then
          LL_REC.N02 := 1;
        when EVENT_TYPE_REBUKE then
          LL_REC.N02 := 2;
        when EVENT_TYPE_ERROR then
          LL_REC.N02 := 3;
      end case;
      if A_CLAIM.RELEASE_TO is null then
        LL_REC.N03 := 0;
      else
        LL_REC.N03 := 1;
      end if;
      LL_REC.N04 := A_CLAIM.STATUDPTYPE;
      LL_REC.N05 := A_CLAIM.PRIORITY;
      LL_REC.N06 := A_CLAIM.NEXISTDOC;
      return LL_REC;
    end;
  begin
    /*    DBMS_PIPE.pack_message('APP_USER_PESRRN = «' || PKG_SESSION_VARS.GET_NUM(SESS_V_PERS_RN) || '»' || CR ||
    'APP_DEPRN = «' || PKG_SESSION_VARS.GET_NUM(SESS_V_DEPT_RN) || '»'
    );
    i := DBMS_PIPE.send_message('TEST');*/
    if SET_SESSION(P_SESSION) then
      if P_NEW_REC_RN is not null then
        open LC_LAST_CLAIM(P_NEW_REC_RN);
        fetch LC_LAST_CLAIM
          into L_CLAIM;
        close LC_LAST_CLAIM;
        if L_CLAIM.RN is not null then
          pipe row(CLAIM_TO_REC(L_CLAIM));
        end if;
      end if;
      L_IDENT := PKG_SESSION_VARS.GET_NUM(SESS_V_COND_IDENT);
      SET_CONDS_(L_IDENT, P_COND_RN);
      open LC_CLAIMS(L_IDENT);
      loop
        fetch LC_CLAIMS
          into L_CLAIM;
        exit when LC_CLAIMS%notfound;
        if (P_NEW_REC_RN is null) then
          pipe row(CLAIM_TO_REC(L_CLAIM));
        elsif L_CLAIM.RN != P_NEW_REC_RN then
          pipe row(CLAIM_TO_REC(L_CLAIM));
        end if;
      end loop;
      close LC_CLAIMS;
    end if;
  end;

  procedure CLAIM_FORMAT_ROW is
  begin
    HTP.PRINT('<li data-icon="false" class="p_nopad"><a href="#" class="p-mob"><h3><div class = "p-icon icon-t' ||
              L_CLAIM.SEVENT_TYPE_CLASS || '"></div>' ||
              trim(L_CLAIM.SEVENT_NUMB) || '<span class="p-norm_black"> ' ||
              L_CLAIM.DREG_DATE_SHORT || ' ' || L_CLAIM.SINIT_PERSON_AGNCODE ||
              '</span></h3>');
    HTP.PRINT('<p class="p-norm_black">' ||
              TO_CHAR(L_CLAIM.DDCHANGE_DATE, 'dd.mm.yyyy') || ' ' ||
              L_CLAIM.SEVENT_STAT || ' ' || L_CLAIM.SEXECUTOR || '</p>');
    HTP.PRINT('<p class="p-norm_black">' || L_CLAIM.SUNITCODE || '</p>');
    HTP.PRINT('<p class="ui-li-aside" style="right: 1em;">' || L_CLAIM.SREL ||
              '</br><span class="p_e_pror ' || L_CLAIM.SPRIORITY_CLASS || '">' ||
              L_CLAIM.NPRIORITY || '</span></p></a></li>');
  end;

  function CLAIM_DOCUM
  (
    P_SESSION in varchar2,
    P_RN      in number
  ) return T_MOB_DOC
    pipelined is
    cursor LC_DOC is
      select FILE_PATH,
             BDATA,
             CDATA
        from FILELINKS M
       where RN = P_RN;
    L_DOC LC_DOC%rowtype;
    L_REC T_MOB_DOC_REC;
  
    function C2B(C clob) return blob is
      LL_BLB          blob;
      LL_DEST_OFFSET  integer;
      LL_SRC_OFFSET   integer;
      LL_LANG_CONTEXT integer;
      LL_WARNING      varchar2(2000);
    begin
      DBMS_LOB.CREATETEMPORARY(LL_BLB, false);
      LL_DEST_OFFSET  := 1;
      LL_SRC_OFFSET   := 1;
      LL_LANG_CONTEXT := 0;
      DBMS_LOB.CONVERTTOBLOB(LL_BLB,
                             C,
                             DBMS_LOB.GETLENGTH(C),
                             LL_DEST_OFFSET,
                             LL_SRC_OFFSET,
                             0,
                             LL_LANG_CONTEXT,
                             LL_WARNING);
      return LL_BLB;
    end;
  begin
    if not SET_SESSION(P_SESSION) then
      return;
    end if;
    open LC_DOC;
    fetch LC_DOC
      into L_DOC;
    close LC_DOC;
    if DBMS_LOB.GETLENGTH(L_DOC.BDATA) > 0 then
      L_REC.CT := L_DOC.BDATA;
    elsif DBMS_LOB.GETLENGTH(L_DOC.CDATA) > 0 then
      L_REC.CT := C2B(L_DOC.CDATA);
    end if;
    L_REC.MT := UDO_GET_FILE_CONTENTTYPE(L_DOC.FILE_PATH);
    pipe row(L_REC);
  end;

  function CLAIM_DOCUMS
  (
    P_SESSION in varchar2,
    P_PRN     in number
  ) return T_MOB_REP
    pipelined is
    cursor LC_DOCS is
      select NRN,
             SFILE_PATH,
             NVL(NSIZE, 0) NSIZE
        from UDO_V_CLAIMS_FILELINKS M
       where NPRN = P_PRN
       order by SCODE;
    L_DOC LC_DOCS%rowtype;
    L_REC T_MOB_REP_REC;
  begin
    if not SET_SESSION(P_SESSION) then
      return;
    end if;
    open LC_DOCS;
    loop
      fetch LC_DOCS
        into L_DOC;
      exit when LC_DOCS%notfound;
      L_REC     := G_EMPTY_REC;
      L_REC.S01 := L_DOC.SFILE_PATH;
      L_REC.N01 := L_DOC.NRN;
      L_REC.N02 := L_DOC.NSIZE;
      pipe row(L_REC);
    end loop;
    close LC_DOCS;
  end;

  function CLAIM_HISTORY
  (
    P_SESSION in varchar2,
    P_RN      in number
  ) return T_MOB_REP
    pipelined is
    FLAG_COMMENT_OTHER  constant varchar2(1) := 'O';
    FLAG_NOCOMMENT      constant varchar2(1) := 'N';
    FLAG_COMMENT_AUTHOR constant varchar2(1) := 'A';
    FLAG_IGNORE         constant varchar2(1) := 'I';
  
    ACT_NOTE constant varchar2(17) := 'CLNEVNOTES_INSERT';
    ACT_FWD  constant varchar2(22) := 'CLNEVENTS_CHANGE_STATE';
    ACT_NULL constant varchar2(15) := 'CLNEVENTS_CLOSE';
    ACT_RET  constant varchar2(16) := 'CLNEVENTS_RETURN';
    ACT_SEND constant varchar2(17) := 'CLNEVENTS_DO_SEND';
    ACT_UPD  constant varchar2(16) := 'CLNEVENTS_UPDATE';
    ACT_INS  constant varchar2(16) := 'CLNEVENTS_INSERT';
  
    ACT_NOTE_N             constant binary_integer := 1;
    ACT_FWD_N              constant binary_integer := 2;
    ACT_NULL_N             constant binary_integer := 3;
    ACT_RET_N              constant binary_integer := 4;
    ACT_SEND_N             constant binary_integer := 5;
    ACT_UPD_N              constant binary_integer := 6;
    ACT_INS_N              constant binary_integer := 7;
    MAX_SHIFT_BETWEEN_NOTE constant number := 1 / 24 / 60 * 5; -- 5 min
  
    cursor LC_HISTORY is
      select 'I' as CFLAG,
             H.DCHANGE_DATE,
             H.SAUTHNAME,
             H.SACTION_CODE,
             0 as NACTION_CODE,
             H.SEVENT_TYPE_NAME,
             H.SSEND,
             H.STEXT
        from UDO_V_CLAIM_HIST H
       where H.NPRN = P_RN
       order by DCHANGE_DATE asc;
    type T_HISTTAB is table of LC_HISTORY%rowtype index by binary_integer;
    L_HISTTAB    T_HISTTAB;
    L_AUTHOR     UDO_V_CLAIM_HIST.SAUTHNAME%type;
    L_LAST_DESCR UDO_V_CLAIM_HIST.STEXT%type;
    L_TRIGGER    boolean;
    L_REC        T_MOB_REP_REC;
  begin
    if not SET_SESSION(P_SESSION) then
      return;
    end if;
  
    open LC_HISTORY;
    fetch LC_HISTORY bulk collect
      into L_HISTTAB;
    close LC_HISTORY;
  
    for I in 1 .. L_HISTTAB.COUNT loop
      case L_HISTTAB(I).SACTION_CODE
        when ACT_INS then
          L_HISTTAB(I).NACTION_CODE := ACT_INS_N;
          L_AUTHOR := L_HISTTAB(I).SAUTHNAME;
          L_HISTTAB(I).CFLAG := FLAG_COMMENT_AUTHOR;
          L_LAST_DESCR := L_HISTTAB(I).STEXT;
        when ACT_UPD then
          L_HISTTAB(I).NACTION_CODE := ACT_UPD_N;
          if (L_HISTTAB(I).STEXT is not null) and
             (L_HISTTAB(I).STEXT != L_LAST_DESCR) then
            L_HISTTAB(I).CFLAG := FLAG_COMMENT_AUTHOR;
            L_LAST_DESCR := L_HISTTAB(I).STEXT;
          end if;
        when ACT_SEND then
          L_HISTTAB(I).NACTION_CODE := ACT_SEND_N;
          if L_HISTTAB(I).CFLAG = FLAG_IGNORE then
            L_HISTTAB(I).CFLAG := FLAG_NOCOMMENT;
          end if;
        when ACT_RET then
          L_HISTTAB(I).NACTION_CODE := ACT_RET_N;
          if L_HISTTAB(I).CFLAG = FLAG_IGNORE then
            L_HISTTAB(I).CFLAG := FLAG_NOCOMMENT;
          end if;
        when ACT_NULL then
          L_HISTTAB(I).NACTION_CODE := ACT_NULL_N;
          if L_HISTTAB(I).CFLAG = FLAG_IGNORE then
            L_HISTTAB(I).CFLAG := FLAG_NOCOMMENT;
          end if;
        when ACT_FWD then
          L_HISTTAB(I).NACTION_CODE := ACT_FWD_N;
          if L_HISTTAB(I).CFLAG = FLAG_IGNORE then
            L_HISTTAB(I).CFLAG := FLAG_NOCOMMENT;
          end if;
        when ACT_NOTE then
          L_HISTTAB(I).NACTION_CODE := ACT_NOTE_N;
          L_TRIGGER := true;
          if I > 1 then
            if ((L_HISTTAB(I - 1)
               .CFLAG = FLAG_NOCOMMENT or L_HISTTAB(I - 1).CFLAG = FLAG_IGNORE) and
               (L_HISTTAB(I - 1).SAUTHNAME = L_HISTTAB(I).SAUTHNAME) and
               ((L_HISTTAB(I).DCHANGE_DATE - L_HISTTAB(I - 1).DCHANGE_DATE) <
               MAX_SHIFT_BETWEEN_NOTE)) then
              L_TRIGGER := false;
              if L_HISTTAB(I).SAUTHNAME = L_AUTHOR then
                L_HISTTAB(I - 1).CFLAG := FLAG_COMMENT_AUTHOR;
              else
                L_HISTTAB(I - 1).CFLAG := FLAG_COMMENT_OTHER;
              end if;
              L_HISTTAB(I - 1).STEXT := L_HISTTAB(I).STEXT;
            end if;
          end if;
          if (I < L_HISTTAB.COUNT) and L_TRIGGER then
            if ((L_HISTTAB(I + 1)
               .CFLAG = FLAG_NOCOMMENT or L_HISTTAB(I + 1).CFLAG = FLAG_IGNORE) and
               (L_HISTTAB(I + 1).SAUTHNAME = L_HISTTAB(I).SAUTHNAME) and
               ((L_HISTTAB(I + 1).DCHANGE_DATE - L_HISTTAB(I).DCHANGE_DATE) <
               MAX_SHIFT_BETWEEN_NOTE)) then
              L_TRIGGER := false;
              if L_HISTTAB(I).SAUTHNAME = L_AUTHOR then
                L_HISTTAB(I + 1).CFLAG := FLAG_COMMENT_AUTHOR;
              else
                L_HISTTAB(I + 1).CFLAG := FLAG_COMMENT_OTHER;
              end if;
              L_HISTTAB(I + 1).STEXT := L_HISTTAB(I).STEXT;
            end if;
          end if;
          if L_TRIGGER then
            if L_HISTTAB(I).SAUTHNAME = L_AUTHOR then
              L_HISTTAB(I).CFLAG := FLAG_COMMENT_AUTHOR;
            else
              L_HISTTAB(I).CFLAG := FLAG_COMMENT_OTHER;
            end if;
          end if;
        else
          null;
      end case;
    end loop;
    for I in 1 .. L_HISTTAB.COUNT loop
      if L_HISTTAB(I).CFLAG != FLAG_IGNORE then
        /* S01-Flag
           S02-Date
           S03-Who
           S04-New state
           S05-Whom
           S06-Text
           N01-Action
        */
        L_REC     := G_EMPTY_REC;
        L_REC.S01 := L_HISTTAB(I).CFLAG;
        L_REC.S02 := D2C_(L_HISTTAB(I).DCHANGE_DATE, 1);
        L_REC.S03 := L_HISTTAB(I).SAUTHNAME;
        L_REC.N01 := L_HISTTAB(I).NACTION_CODE;
        if L_HISTTAB(I).NACTION_CODE in (ACT_FWD_N, ACT_RET_N) then
          L_REC.S04 := L_HISTTAB(I).SEVENT_TYPE_NAME;
        end if;
        if L_HISTTAB(I).NACTION_CODE in (ACT_FWD_N, ACT_RET_N, ACT_SEND_N) then
          L_REC.S05 := L_HISTTAB(I).SSEND;
        end if;
        L_REC.S06 := L_HISTTAB(I).STEXT;
        pipe row(L_REC);
      end if;
    end loop;
  end;

  procedure CLAIM_GET_LIST is
    cursor LC_CLAIMS is
      select *
        from UDO_V_CLAIMS_APEX
       order by NRN desc;
    CNT binary_integer;
  begin
    open LC_CLAIMS;
    CNT := 1;
    HTP.PRINT('<ul data-role="listview" class="p-mob">');
    loop
      fetch LC_CLAIMS
        into L_CLAIM;
      CNT := CNT + 1;
      exit when LC_CLAIMS%notfound or CNT > 100;
      CLAIM_FORMAT_ROW;
    end loop;
    HTP.PRINT('</ul>');
    close LC_CLAIMS;
  end;

begin
  null;
end UDO_PKG_MOBILE_IFACE;

/
