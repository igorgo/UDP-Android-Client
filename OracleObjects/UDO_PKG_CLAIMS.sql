CREATE OR REPLACE PACKAGE UDO_PKG_CLAIMS is

  function GET_SELF_PMO_PERFORMER return varchar2;

  procedure GET_USERPERFORM
  (
    NPERSON     out number,
    SAGENT      out varchar2,
    NDEPARTMENT out number,
    SDEPARTMENT out varchar2
  );

  procedure CLAIM_INSERT
  (
    NCRN                in number,
    SEVENT_TYPE         in varchar2,
    SLINKED_CLAIM       in varchar2,
    NPRIORITY           in number,
    NSEND_TO_DEVELOPERS in number,
    SINIT_PERSON        in varchar2,
    SMODULE             in varchar2,
    SUNITCODE           in varchar2,
    SUNITFUNC           in varchar2,
    SEVENT_DESCR        in varchar2,
    SREL_FROM           in varchar2,
    SBUILD_FROM         in varchar2,
    SREL_TO             in varchar2,
    NRN                 out number
  );

  procedure CLAIM_UPDATE
  (
    NRN           in number,
    SLINKED_CLAIM in varchar2,
    SEVENT_DESCR  in varchar2,
    SREL_FROM     in varchar2,
    SBUILD_FROM   in varchar2,
    SREL_TO       in varchar2,
    SBUILD_TO     in varchar2,
    SMODULE       in varchar2,
    SUNITCODE     in varchar2,
    SUNITFUNC     in varchar2
  );

  procedure CLAIM_DELETE(NRN in number);

  procedure CLAIM_CHANGE_STATE
  (
    NRN            in number,
    SEVENT_TYPE    in varchar2,
    SEVENT_STAT    in varchar2,
    SSEND_CLIENT   in varchar2,
    SSEND_DIVISION in varchar2,
    SSEND_POST     in varchar2,
    SSEND_PERFORM  in varchar2,
    SSEND_PERSON   in varchar2,
    SNOTE_HEADER   in varchar2,
    SNOTE          in varchar2,
    NPRIORITY      in number,
    SREL_TO        in varchar2,
    SBUILD_TO      in varchar2
  );

  procedure CLAIM_DO_SEND
  (
    NRN            in number,
    SEVENT_TYPE    in varchar2,
    SEVENT_STAT    in varchar2,
    SSEND_CLIENT   in varchar2,
    SSEND_DIVISION in varchar2,
    SSEND_POST     in varchar2,
    SSEND_PERFORM  in varchar2,
    SSEND_PERSON   in varchar2,
    SNOTE_HEADER   in varchar2,
    SNOTE          in varchar2
  );

  procedure CLAIM_RETURN
  (
    NRN          in number,
    SNOTE_HEADER in varchar2,
    SNOTE        in varchar2
  );

  procedure CLAIM_CLOSE(NRN in number);

  procedure CLAIM_HELPSIGN_NEED
  (
    NRN     in number,
    NSTATUS in number,
    SNOTE   in varchar2
  );

  procedure CLAIM_HELPSIGN_STAT
  (
    NRN     in number,
    NSTATUS in number
  );

  procedure CLAIM_ADD_LINKDOC(NCLAIM_RN in number,
                              --NLINKDOC_BUFFER in number,
                              BTEMPLATE     in blob,
                              SLINKDOC_TYPE in varchar2,
                              SLINKDOC_PATH in varchar2);

  function GET_CURRENT_PERSON_NAME return varchar2;
  function GET_CURRENT_PERSON_RN return number;
  procedure PREPARE_HISTORY_TO_WEB
  (
    NEVENT      in number,
    NHAS_DOCUMS out number
  );

  procedure GET_AVAIL_ACTIONS
  (
    NRN       in number,
    NUPDATE   out number,
    NDELETE   out number,
    NSTATE    out number,
    NSEND     out number,
    NRETURN   out number,
    NCLOSE    out number,
    NADDNOTE  out number,
    NADDDOCUM out number
  );

  procedure GET_CURRENT_RELEASES
  (
    STAB_R out number,
    STAB_B out number,
    BETA_R out number,
    BETA_B out number
  );

end UDO_PKG_CLAIMS;


/


CREATE OR REPLACE PACKAGE BODY UDO_PKG_CLAIMS is

  C_PMO_DEPT_RN             constant INS_DEPARTMENT.RN%type := 1664602; -- подразделение ПМО
  C_DEFAULT_EVENT_CATALOG   constant ACATALOG.RN%type := 34277;
  C_DEFAULT_LINKDOC_CATALOG constant ACATALOG.RN%type := 547;
  C_DEFAULT_EVENT_STATUS    constant CLNEVNSTATS.EVNSTAT_CODE%type := 'Инициировано';
  --  C_COMPANY                 constant COMPANIES.RN%type := 544;
  C_DEFAULT_NOTE_HEADER  constant CLNEVNTNOTETYPES.CODE%type := 'Примечание';
  C_DDOC_NOTE_HEADER     constant CLNEVNTNOTETYPES.CODE%type := 'Документирование';
  C_DEFAULT_LINKDOC_TYPE constant varchar2(20) := 'ПД';
  C_EVENTS_UNITCODE      constant UNITLIST.UNITCODE%type := 'ClientEvents';
  C_LAST_HIST_TIME       constant varchar2(40) := 'CLAIM_LAST_HIST_TIME';
  C_CURRENT_PERSON_CODE  constant varchar2(40) := 'CLAIM_CURRENT_PERSON_CODE';
  C_CURRENT_PERSON_RN    constant varchar2(40) := 'CLAIM_CURRENT_PERSON_RN';
  C_CURRENT_PERSON_NAME  constant varchar2(40) := 'CLAIM_CURRENT_PERSON_NAME';
  --  LAST_HIST_TIME      date;
  --  CURRENT_PERSON_CODE CLNPERSONS.CODE%type;
  -- CURRENT_PERSON_RN CLNPERSONS.RN%type;
  TMP_STR varchar2(4000);
  TMP_NUM number;

  procedure SET_LAST_HIST_TIME(DTIME date default sysdate) is
  begin
    PKG_SESSION_VARS.PUT(C_LAST_HIST_TIME, DTIME);
  end SET_LAST_HIST_TIME;

  function GET_LAST_HIST_TIME return date is
  begin
    return PKG_SESSION_VARS.GET_DAT(C_LAST_HIST_TIME, sysdate);
  end GET_LAST_HIST_TIME;

  function GET_CURRENT_PERSON_CODE return varchar2 is
    SRESULT varchar2(255);
  begin
    SRESULT := PKG_SESSION_VARS.GET_STR(C_CURRENT_PERSON_CODE);
    if SRESULT is null then
      FIND_PERSON_AUTHID(SRESULT, TMP_STR, TMP_STR, TMP_STR);
      PKG_SESSION_VARS.PUT(C_CURRENT_PERSON_CODE, SRESULT);
    end if;
    return SRESULT;
  end;

  function GET_CURRENT_PERSON_RN return number is
    NRESULT number;
  begin
    NRESULT := PKG_SESSION_VARS.GET_NUM(C_CURRENT_PERSON_RN);
    if NRESULT is null then
      FIND_CLNPERSONS_CODE(NFLAG_SMART  => 1,
                           NFLAG_OPTION => 0,
                           NCOMPANY     => PKG_SESSION.GET_COMPANY,
                           SCODE        => GET_CURRENT_PERSON_CODE,
                           NRN          => NRESULT);
      PKG_SESSION_VARS.PUT(C_CURRENT_PERSON_RN, NRESULT);
    end if;
    return NRESULT;
  end;

  function GET_CURRENT_PERSON_NAME return varchar2 is
    cursor LC_USERNAME is
      select A.AGNNAME
        from AGNLIST    A,
             CLNPERSONS P
       where A.RN = P.PERS_AGENT
         and P.RN = GET_CURRENT_PERSON_RN;
    L_USERNAME AGNLIST.AGNNAME%type;
  begin
    L_USERNAME := PKG_SESSION_VARS.GET_STR(C_CURRENT_PERSON_NAME);
    if L_USERNAME is null then
      open LC_USERNAME;
      fetch LC_USERNAME
        into L_USERNAME;
      close LC_USERNAME;
      PKG_SESSION_VARS.PUT(C_CURRENT_PERSON_NAME, L_USERNAME);
    end if;
    return L_USERNAME;
  end;

  procedure WAIT(NSEC in number default 1) is
    L_CURTIME date;
  begin
    L_CURTIME := GET_LAST_HIST_TIME + NSEC / 86400; -- / 86400 = (24 * 60 * 60)
    while sysdate < L_CURTIME loop
      null;
    end loop;
  end WAIT;

  function GET_DEVELOPERS_START_POINT(SEVENT_TYPE in varchar2) return varchar2 is
  begin
    case SEVENT_TYPE
      when 'ДОРАБОТКА' then
        return 'На рассмотрении';
      when 'ЗАМЕЧАНИЕ' then
        return 'ТестПроверка';
      when 'ОШИБКА' then
        return 'ТестПроверка';
      else
        return null;
    end case;
  end GET_DEVELOPERS_START_POINT;

  procedure GET_CLAIM_NEXTNUMB(
                               --    NRELEASE    in number,
                               SEVENT_PREF out varchar2,
                               SEVENT_NUMB out varchar2) is
  begin
    -- генирим префикс и номер
    /*    select replace(trim(SOFTVERSION), '.')
     into SEVENT_PREF
     from UDO_SOFTRELEASES
    where RN = NRELEASE;*/
    SEVENT_PREF := 'P8';
    SEVENT_NUMB := GET_CLNEVENTS_NEXTNUMB(PKG_SESSION.GET_COMPANY, SEVENT_PREF);
  end GET_CLAIM_NEXTNUMB;

  function GET_CLAIM_BY_NUMB(SFULL_CLAIM_NUMB in varchar2) return number is
    L_RN   CLNEVENTS.RN%type;
    L_PREF CLNEVENTS.EVENT_PREF%type;
    L_NUMB CLNEVENTS.EVENT_NUMB%type;
  begin
    L_PREF := LPAD(STRTOK(SFULL_CLAIM_NUMB, '-', 1), 10, ' ');
    L_NUMB := LPAD(STRTOK(SFULL_CLAIM_NUMB, '-', 2), 10, ' ');

    select RN
      into L_RN
      from CLNEVENTS
     where EVENT_PREF = L_PREF
       and EVENT_NUMB = L_NUMB
       and COMPANY = PKG_SESSION.GET_COMPANY;

    return L_RN;
  end GET_CLAIM_BY_NUMB;

  procedure AUTOSEND_TO_DEVELOPERS(NEVENT_RN in number) is
    L_SEVENT_TYPE CLNEVNTYPES.EVNTYPE_CODE%type;
  begin
    select ET.EVNTYPE_CODE
      into L_SEVENT_TYPE
      from CLNEVENTS   E,
           CLNEVNTYPES ET
     where E.RN = NEVENT_RN
       and E.EVENT_TYPE = ET.RN;

    WAIT;
    CLAIM_CHANGE_STATE(NRN            => NEVENT_RN,
                       SEVENT_TYPE    => L_SEVENT_TYPE,
                       SEVENT_STAT    => GET_DEVELOPERS_START_POINT(L_SEVENT_TYPE),
                       SSEND_CLIENT   => null,
                       SSEND_DIVISION => null,
                       SSEND_POST     => null,
                       SSEND_PERFORM  => null,
                       SSEND_PERSON   => null,
                       SNOTE_HEADER   => null,
                       SNOTE          => null,
                       NPRIORITY      => null,
                       SREL_TO        => null,
                       SBUILD_TO      => null);

    SET_LAST_HIST_TIME;
  end AUTOSEND_TO_DEVELOPERS;

  procedure CLAIM_JOINS
  (
    SEVENT_TYPE     in varchar2,
    SEVENT_STAT     in varchar2,
    SSEND_CLIENT    in varchar2,
    SSEND_DIVISION  in varchar2,
    SSEND_POST      in varchar2,
    SSEND_PERFORM   in varchar2,
    SSEND_PERSON    in varchar2,
    SINIT_PERSON    in varchar2,
    SCURREL         in varchar2,
    SCURBLD         in varchar2,
    SREL            in varchar2,
    SBLD            in varchar2,
    NEVENT_TYPE     out number,
    NEVENT_STAT     out number,
    NSEND_CLIENT    out number,
    NSEND_DIVISION  out number,
    NSEND_POST      out number,
    NSEND_PERFORM   out number,
    NSEND_PERSON    out number,
    NINIT_PERSON    out number,
    NCURREL         out number,
    NCURBLD         out number,
    NREL            out number,
    NBLD            out number,
    NSEEK_DEF_STATE in number default 0
  ) is
  begin
    /* инициатор */
    if RTRIM(SINIT_PERSON) is not null then
      FIND_CLNPERSONS_CODE(0, 0, PKG_SESSION.GET_COMPANY, SINIT_PERSON, NINIT_PERSON);
    else
      NINIT_PERSON := null;
    end if;

    /* тип события */
    if (RTRIM(SEVENT_TYPE) is not null) then
      FIND_CLNEVNTYPES_CODE(0, PKG_SESSION.GET_COMPANY, SEVENT_TYPE, NEVENT_TYPE);
    else
      NEVENT_TYPE := null;
    end if;

    /* статус события */
    if (RTRIM(SEVENT_STAT) is not null) then
      FIND_CLNEVNTYPSTS_CODE(0, PKG_SESSION.GET_COMPANY, SEVENT_TYPE, SEVENT_STAT, NEVENT_STAT);
    else
      if NSEEK_DEF_STATE = 1 then
        NEVENT_STAT := GET_CLNEVNTYPSTS_BASE_DEFAULT(0, NEVENT_TYPE);
      else
        NEVENT_STAT := null;
      end if;
    end if;

    /* направить-организация */
    if (RTRIM(SSEND_CLIENT) is not null) then
      FIND_CLNCLIENTS_CODE(0, PKG_SESSION.GET_COMPANY, SSEND_CLIENT, NSEND_CLIENT);
    else
      NSEND_CLIENT := null;
    end if;

    /* направить-подразделение */
    if (RTRIM(SSEND_DIVISION) is not null) then
      FIND_SUBDIVS_CODE(0, PKG_SESSION.GET_COMPANY, SSEND_DIVISION, NSEND_DIVISION);
    else
      NSEND_DIVISION := null;
    end if;

    /* направить-должность */
    if (RTRIM(SSEND_POST) is not null) then
      FIND_CLNPOSTS_CODE(0, PKG_SESSION.GET_COMPANY, SSEND_POST, NSEND_POST);
    else
      NSEND_POST := null;
    end if;

    /* направить-должность в подразделении */
    if (RTRIM(SSEND_PERFORM) is not null) then
      FIND_CLNPSDEP_CODE(0, 0, PKG_SESSION.GET_COMPANY, SSEND_PERFORM, NSEND_PERFORM);
    else
      NSEND_PERFORM := null;
    end if;

    /* направить-сотрудник */
    if (RTRIM(SSEND_PERSON) is not null) then
      FIND_CLNPERSONS_CODE(0, 0, PKG_SESSION.GET_COMPANY, SSEND_PERSON, NSEND_PERSON);
    else
      NSEND_PERSON := null;
    end if;

    /* релиз обнаружения */
    UDO_FIND_SOFTRELEASES_NAME(0, 1, PKG_SESSION.GET_COMPANY, SCURREL, NCURREL);
    /* релиз включения */
    UDO_FIND_SOFTRELEASES_NAME(0, 1, PKG_SESSION.GET_COMPANY, SREL, NREL);

    /* сборка обнаружения */
    if NCURREL is not null then
      UDO_FIND_SOFTBUILDS_NAME(0, 1, PKG_SESSION.GET_COMPANY, NCURREL, SCURREL, SCURBLD, NCURBLD);
    else
      NCURBLD := null;
    end if;

    /* сборка включения */
    if NREL is not null then
      UDO_FIND_SOFTBUILDS_NAME(0, 1, PKG_SESSION.GET_COMPANY, NREL, SREL, SBLD, NBLD);
    else
      NBLD := null;
    end if;
  end CLAIM_JOINS;

  procedure CLAIM_SET_DEPENDENCE
  (
    SIN_CLAIM  in varchar2,
    SOUT_CLAIM in varchar2,
    NOUT_CLAIM in number
  ) is
    L_IN_CLAIM_RN CLNEVENTS.RN%type;
    L_OUT_CLAIM   varchar2(21);
  begin
    L_IN_CLAIM_RN := GET_CLAIM_BY_NUMB(SIN_CLAIM);
    if L_IN_CLAIM_RN is not null then
      P_LINKSALL_LINK_DIRECT(PKG_SESSION.GET_COMPANY,
                             C_EVENTS_UNITCODE,
                             L_IN_CLAIM_RN,
                             null,
                             sysdate,
                             0,
                             C_EVENTS_UNITCODE,
                             NOUT_CLAIM,
                             null,
                             sysdate,
                             0,
                             0);

      if SOUT_CLAIM is null then
        select trim(E.EVENT_PREF) || '-' || trim(E.EVENT_NUMB)
          into L_OUT_CLAIM
          from CLNEVENTS E
         where E.RN = NOUT_CLAIM;
      else
        L_OUT_CLAIM := SOUT_CLAIM;
      end if;

      P_CLNEVNOTES_INSERT(NCOMPANY     => PKG_SESSION.GET_COMPANY,
                          NPRN         => L_IN_CLAIM_RN,
                          SCLIENT      => null,
                          SAUTHID      => null,
                          SNOTE_HEADER => C_DEFAULT_NOTE_HEADER,
                          SNOTE        => 'По данному событию, ' || TO_CHAR(sysdate, 'DD.MM.YYYY') ||
                                          ', сформировано событие: "' || L_OUT_CLAIM || '"',
                          NRN          => TMP_NUM);
      WAIT;
      P_CLNEVNOTES_INSERT(NCOMPANY     => PKG_SESSION.GET_COMPANY,
                          NPRN         => NOUT_CLAIM,
                          SCLIENT      => null,
                          SAUTHID      => null,
                          SNOTE_HEADER => C_DEFAULT_NOTE_HEADER,
                          SNOTE        => 'Основано на событии: "' || SIN_CLAIM || '"',
                          NRN          => TMP_NUM);
      SET_LAST_HIST_TIME;
    end if;
  end CLAIM_SET_DEPENDENCE;

  procedure CLAIM_BASE_INSERT
  (
    NCRN           in number,
    SEVENT_PREF    in varchar2,
    SEVENT_NUMB    in varchar2,
    NEVENT_TYPE    in number,
    NEVENT_STAT    in number,
    NINIT_PERSON   in number,
    NCLIENT_PERSON in number,
    NCLIENT_CLIENT in number,
    NSEND_DIVISION in number,
    NSEND_PERSON   in number,
    SEVENT_DESCR   in varchar2,
    NFROM_RELEASE  in number,
    NFROM_BUILD    in number,
    NTO_RELEASE    in number,
    NTO_BUILD      in number,
    SMODULE        in varchar2,
    SUNITCODE      in varchar2,
    SUNITFUNC      in varchar2,
    NRN            out number
  ) is
  begin

    P_CLNEVENTS_BASE_INSERT(NCOMPANY       => PKG_SESSION.GET_COMPANY,
                            NCRN           => NCRN,
                            SEVENT_PREF    => SEVENT_PREF,
                            SEVENT_NUMB    => SEVENT_NUMB,
                            NEVENT_TYPE    => NEVENT_TYPE,
                            NEVENT_STAT    => NEVENT_STAT,
                            DPLAN_DATE     => null,
                            NINIT_PERSON   => NINIT_PERSON,
                            NCLIENT_CLIENT => NCLIENT_CLIENT,
                            NCLIENT_PERSON => NCLIENT_PERSON,
                            NSEND_CLIENT   => null,
                            NSEND_DIVISION => NSEND_DIVISION,
                            NSEND_POST     => null,
                            NSEND_PERFORM  => null,
                            NSEND_PERSON   => NSEND_PERSON,
                            SEVENT_DESCR   => SEVENT_DESCR,
                            SREASON        => null,
                            NRN            => NRN);

    SET_LAST_HIST_TIME;
    insert into CLNEVENTS_EXT
      (PRN, SOFTRELEASE, SOFTBUILD, CUR_SOFTRELEASE, CUR_SOFTBUILD, UNITCODE, ACODE, FCODE)
    values
      (NRN, NTO_RELEASE, NTO_BUILD, NFROM_RELEASE, NFROM_BUILD, SUNITCODE, SMODULE, SUNITFUNC);
  end CLAIM_BASE_INSERT;

  procedure CLAIM_BASE_UPDATE
  (
    NRN           in number,
    SEVENT_DESCR  in varchar2,
    NFROM_RELEASE in number,
    NFROM_BUILD   in number,
    NTO_RELEASE   in number,
    NTO_BUILD     in number,
    SMODULE       in varchar2,
    SUNITCODE     in varchar2,
    SUNITFUNC     in varchar2
  ) is
    L_CLAIM_REC CLNEVENTS%rowtype;
  begin

    select *
      into L_CLAIM_REC
      from CLNEVENTS
     where RN = NRN;

    P_CLNEVENTS_BASE_UPDATE(NCOMPANY       => PKG_SESSION.GET_COMPANY,
                            NRN            => NRN,
                            SACTION_CODE   => 'CLNEVENTS_UPDATE',
                            NEVENT_STAT    => L_CLAIM_REC.EVENT_STAT,
                            NCLIENT_CLIENT => L_CLAIM_REC.CLIENT_CLIENT,
                            NCLIENT_PERSON => L_CLAIM_REC.CLIENT_PERSON,
                            NSEND_CLIENT   => L_CLAIM_REC.SEND_CLIENT,
                            NSEND_DIVISION => L_CLAIM_REC.SEND_DIVISION,
                            NSEND_POST     => L_CLAIM_REC.SEND_POST,
                            NSEND_PERFORM  => L_CLAIM_REC.SEND_PERFORM,
                            NSEND_PERSON   => L_CLAIM_REC.SEND_PERSON,
                            SEVENT_DESCR   => SEVENT_DESCR);

    SET_LAST_HIST_TIME;
    update CLNEVENTS_EXT
       set CUR_SOFTRELEASE = NFROM_RELEASE,
           CUR_SOFTBUILD   = NFROM_BUILD,
           SOFTRELEASE     = NTO_RELEASE,
           SOFTBUILD       = NTO_BUILD,
           UNITCODE        = SUNITCODE,
           ACODE           = SMODULE,
           FCODE           = SUNITFUNC
     where PRN = NRN;
  end CLAIM_BASE_UPDATE;

  function GET_SELF_PMO_PERFORMER return varchar2 is
    L_PMO_PERSON_FLAG number(1) := 0;

  begin
    -- проверяем работает ли текущий сотрудник в ПМО?
    select count(*)
      into L_PMO_PERSON_FLAG
      from DUAL
     where exists (select *
              from CLNPSPFM
             where PERSRN = GET_CURRENT_PERSON_RN
               and DEPTRN = C_PMO_DEPT_RN
               and ENDENG is null);
    -- если текущий сотрудник из ПМО, то ему и посылаем событие
    -- (если - нет, то исполнителю по умолчанию)
    if L_PMO_PERSON_FLAG != 0 then
      return GET_CURRENT_PERSON_CODE;
    else
      return null;
    end if;
  end GET_SELF_PMO_PERFORMER;

  procedure GET_USERPERFORM
  (
    NPERSON     out number,
    SAGENT      out varchar2,
    NDEPARTMENT out number,
    SDEPARTMENT out varchar2
  ) is
    cursor L_CUR is
      select P.RN,
             A.AGNABBR,
             D.RN,
             D.CODE
        from CLNPERSONS     P,
             AGNLIST        A,
             CLNPSPFM       PF,
             INS_DEPARTMENT D
       where P.PERS_AGENT = A.RN
         and PERS_AUTHID = UTILIZER
         and PF.PERSRN = P.RN
         and PF.DEPTRN = D.RN(+)
         and ((PF.ENDENG > sysdate) or (PF.ENDENG is null));
  begin
    open L_CUR;
    fetch L_CUR
      into NPERSON,
           SAGENT,
           NDEPARTMENT,
           SDEPARTMENT;
    close L_CUR;
  end;

  procedure CLAIM_INSERT
  (
    NCRN                in number,
    SEVENT_TYPE         in varchar2,
    SLINKED_CLAIM       in varchar2,
    NPRIORITY           in number,
    NSEND_TO_DEVELOPERS in number,
    SINIT_PERSON        in varchar2,
    SMODULE             in varchar2,
    SUNITCODE           in varchar2,
    SUNITFUNC           in varchar2,
    SEVENT_DESCR        in varchar2,
    SREL_FROM           in varchar2,
    SBUILD_FROM         in varchar2,
    SREL_TO             in varchar2,
    NRN                 out number
  ) is
    L_EVENT_PREF      CLNEVENTS.EVENT_PREF%type;
    L_EVENT_NUMB      CLNEVENTS.EVENT_NUMB%type;
    L_FROM_RELEASE_RN CLNEVENTS_EXT.CUR_SOFTRELEASE%type;
    L_FROM_BUILD_RN   CLNEVENTS_EXT.CUR_SOFTBUILD%type;
    L_TO_RELEASE_RN   CLNEVENTS_EXT.SOFTRELEASE%type;
    L_TO_BUILD_RN     CLNEVENTS_EXT.SOFTBUILD%type;
    L_CRN             ACATALOG.RN%type;
    L_EVENT_TYPE      CLNEVENTS.EVENT_TYPE%type;
    L_EVENT_STAT      CLNEVENTS.EVENT_STAT%type;
    L_SEND_PERSON     CLNEVENTS.SEND_PERSON%type;
    L_SEND_DIVISION   CLNEVENTS.SEND_DIVISION%type;

    L_INIT_PERSON   CLNEVENTS.INIT_PERSON%type;
    L_CLIENT_PERSON CLNEVENTS.CLIENT_PERSON%type;
    L_CLIENT_CLIENT CLNEVENTS.CLIENT_CLIENT%type;
  begin

    L_CRN := NVL(NCRN, C_DEFAULT_EVENT_CATALOG);
    -- фиксация начала выполнения действия
    PKG_ENV.PROLOGUE(PKG_SESSION.GET_COMPANY, null, L_CRN, 'ClientEvents', 'CLAIM_INSERT', 'CLNEVENTS');

    CLAIM_JOINS(SEVENT_TYPE    => SEVENT_TYPE,
                SEVENT_STAT    => C_DEFAULT_EVENT_STATUS,
                SSEND_CLIENT   => null,
                SSEND_DIVISION => null,
                SSEND_POST     => null,
                SSEND_PERFORM  => null,
                SSEND_PERSON   => GET_SELF_PMO_PERFORMER,
                SINIT_PERSON   => NVL(SINIT_PERSON, GET_CURRENT_PERSON_CODE),
                SCURREL        => SREL_FROM,
                SCURBLD        => SBUILD_FROM,
                SREL           => SREL_TO,
                SBLD           => null,

                NEVENT_TYPE     => L_EVENT_TYPE,
                NEVENT_STAT     => L_EVENT_STAT,
                NSEND_CLIENT    => TMP_NUM,
                NSEND_DIVISION  => TMP_NUM,
                NSEND_POST      => TMP_NUM,
                NSEND_PERFORM   => TMP_NUM,
                NSEND_PERSON    => L_SEND_PERSON,
                NINIT_PERSON    => L_INIT_PERSON,
                NCURREL         => L_FROM_RELEASE_RN,
                NCURBLD         => L_FROM_BUILD_RN,
                NREL            => L_TO_RELEASE_RN,
                NBLD            => L_TO_BUILD_RN,
                NSEEK_DEF_STATE => 1);

    L_CLIENT_PERSON := L_INIT_PERSON;

    GET_CLAIM_NEXTNUMB( /*L_FROM_RELEASE_RN,*/ L_EVENT_PREF, L_EVENT_NUMB);

    -- поиск исполнителя
    if L_SEND_PERSON is null then
      FIND_EVRTPOINTS_DEFAULT_EXEC(PKG_SESSION.GET_COMPANY,
                                   L_INIT_PERSON,
                                   F_CLNCLIENT_BY_PERSON(PKG_SESSION.GET_COMPANY, L_CLIENT_PERSON),
                                   L_EVENT_TYPE,
                                   L_EVENT_STAT,
                                   L_CLIENT_CLIENT,
                                   L_SEND_DIVISION,
                                   TMP_NUM,
                                   TMP_NUM,
                                   L_SEND_PERSON);
    end if;

    CLAIM_BASE_INSERT(NCRN           => L_CRN,
                      SEVENT_PREF    => L_EVENT_PREF,
                      SEVENT_NUMB    => L_EVENT_NUMB,
                      NEVENT_TYPE    => L_EVENT_TYPE,
                      NEVENT_STAT    => L_EVENT_STAT,
                      NINIT_PERSON   => L_INIT_PERSON,
                      NSEND_DIVISION => L_SEND_DIVISION,
                      NSEND_PERSON   => L_SEND_PERSON,
                      NCLIENT_PERSON => L_CLIENT_PERSON,
                      NCLIENT_CLIENT => L_CLIENT_CLIENT,
                      SEVENT_DESCR   => SEVENT_DESCR,
                      NFROM_RELEASE  => L_FROM_RELEASE_RN,
                      NFROM_BUILD    => L_FROM_BUILD_RN,
                      NTO_RELEASE    => L_TO_RELEASE_RN,
                      NTO_BUILD      => L_TO_BUILD_RN,
                      SMODULE        => SMODULE,
                      SUNITCODE      => SUNITCODE,
                      SUNITFUNC      => SUNITFUNC,
                      NRN            => NRN);

    if NPRIORITY != 5 then
      P_CLNEVENTS_SET_PRIORITY(PKG_SESSION.GET_COMPANY, NRN, NPRIORITY);
    end if;

    if NSEND_TO_DEVELOPERS = 1 then
      AUTOSEND_TO_DEVELOPERS(NRN);
    end if;

    if SLINKED_CLAIM is not null then
      CLAIM_SET_DEPENDENCE(SLINKED_CLAIM, L_EVENT_PREF || '-' || L_EVENT_NUMB, NRN);
    end if;

    PKG_ENV.EPILOGUE(PKG_SESSION.GET_COMPANY, null, L_CRN, 'ClientEvents', 'CLAIM_INSERT', 'CLNEVENTS', NRN);

  end CLAIM_INSERT;

  procedure CLAIM_UPDATE
  (
    NRN           in number,
    SLINKED_CLAIM in varchar2,
    SEVENT_DESCR  in varchar2,
    SREL_FROM     in varchar2,
    SBUILD_FROM   in varchar2,
    SREL_TO       in varchar2,
    SBUILD_TO     in varchar2,
    SMODULE       in varchar2,
    SUNITCODE     in varchar2,
    SUNITFUNC     in varchar2
  ) is
    L_CRN             ACATALOG.RN%type;
    L_FROM_RELEASE_RN CLNEVENTS_EXT.CUR_SOFTRELEASE%type;
    L_FROM_BUILD_RN   CLNEVENTS_EXT.CUR_SOFTBUILD%type;
    L_TO_RELEASE_RN   CLNEVENTS_EXT.SOFTRELEASE%type;
    L_TO_BUILD_RN     CLNEVENTS_EXT.SOFTBUILD%type;
  begin
    P_CLNEVENTS_EXISTS(PKG_SESSION.GET_COMPANY, NRN, L_CRN);

    -- фиксация начала выполнения действия
    PKG_ENV.PROLOGUE(PKG_SESSION.GET_COMPANY, null, L_CRN, 'ClientEvents', 'CLAIM_UPDATE', 'CLNEVENTS', NRN);

    CLAIM_JOINS(SEVENT_TYPE    => null,
                SEVENT_STAT    => null,
                SSEND_CLIENT   => null,
                SSEND_DIVISION => null,
                SSEND_POST     => null,
                SSEND_PERFORM  => null,
                SSEND_PERSON   => null,
                SINIT_PERSON   => null,
                SCURREL        => SREL_FROM,
                SCURBLD        => SBUILD_FROM,
                SREL           => SREL_TO,
                SBLD           => SBUILD_TO,
                NEVENT_TYPE    => TMP_NUM,
                NEVENT_STAT    => TMP_NUM,
                NSEND_CLIENT   => TMP_NUM,
                NSEND_DIVISION => TMP_NUM,
                NSEND_POST     => TMP_NUM,
                NSEND_PERFORM  => TMP_NUM,
                NSEND_PERSON   => TMP_NUM,
                NINIT_PERSON   => TMP_NUM,
                NCURREL        => L_FROM_RELEASE_RN,
                NCURBLD        => L_FROM_BUILD_RN,
                NREL           => L_TO_RELEASE_RN,
                NBLD           => L_TO_BUILD_RN);

    CLAIM_BASE_UPDATE(NRN,
                      SEVENT_DESCR,
                      L_FROM_RELEASE_RN,
                      L_FROM_BUILD_RN,
                      L_TO_RELEASE_RN,
                      L_TO_BUILD_RN,
                      SMODULE,
                      SUNITCODE,
                      SUNITFUNC);

    PKG_ENV.EPILOGUE(PKG_SESSION.GET_COMPANY, null, L_CRN, 'ClientEvents', 'CLAIM_UPDATE', 'CLNEVENTS', NRN);
    if SLINKED_CLAIM is not null then
      CLAIM_SET_DEPENDENCE(SLINKED_CLAIM, null, NRN);
    end if;
  end CLAIM_UPDATE;

  procedure CLAIM_DELETE(NRN in number) is
    L_CRN ACATALOG.RN%type;
  begin
    /* считывание записи */
    P_CLNEVENTS_EXISTS(PKG_SESSION.GET_COMPANY, NRN, L_CRN);

    /* проверка допустимости удаления (по параметру) */
    if GET_OPTIONS_NUM('ClientEvents_Check_InitPerson', PKG_SESSION.GET_COMPANY) = 1 then
      P_CLNEVENTS_CHECK_DELETE(PKG_SESSION.GET_COMPANY, NRN);
    end if;

    /* фиксация начала выполнения действия */
    PKG_ENV.PROLOGUE(PKG_SESSION.GET_COMPANY, null, L_CRN, 'ClientEvents', 'CLAIM_DELETE', 'CLNEVENTS', NRN);

    /* базовое удаление */
    P_CLNEVENTS_BASE_DELETE(NRN);

    /* фиксация окончания выполнения действия */
    PKG_ENV.EPILOGUE(PKG_SESSION.GET_COMPANY, null, L_CRN, 'ClientEvents', 'CLAIM_DELETE', 'CLNEVENTS', NRN);
  end CLAIM_DELETE;

  procedure CLAIM_CHANGE_STATE
  (
    NRN            in number,
    SEVENT_TYPE    in varchar2,
    SEVENT_STAT    in varchar2,
    SSEND_CLIENT   in varchar2,
    SSEND_DIVISION in varchar2,
    SSEND_POST     in varchar2,
    SSEND_PERFORM  in varchar2,
    SSEND_PERSON   in varchar2,
    SNOTE_HEADER   in varchar2,
    SNOTE          in varchar2,
    NPRIORITY      in number,
    SREL_TO        in varchar2,
    SBUILD_TO      in varchar2
  ) is
    L_CRN                   ACATALOG.RN%type;
    L_EVENT                 CLNEVENTS%rowtype;
    L_EVENT_EXT             CLNEVENTS_EXT%rowtype;
    L_ROUTE                 EVROUTES.RN%type;
    L_ROUTE_POINT           EVRTPOINTS.RN%type;
    L_NEXT_POINT            EVRTPOINTS.RN%type;
    L_EVENT_STATUS          CLNEVENTS.EVENT_STAT%type;
    L_EVENT_TYPE            CLNEVENTS.EVENT_TYPE%type;
    L_PASS_COND_PROC        EVRTPTPASS.PASS_COND_PROC%type;
    L_PASS_ERROR_MSG        EVRTPTPASS.PASS_ERROR_MSG%type;
    L_SPASS_COND_PROC       USERPROCS.BLOCKTEXT%type;
    L_PASS_COND_PROC_RESULT number(1);
    L_SEND_CLIENT           CLNEVENTS.SEND_CLIENT%type;
    L_SEND_DIVISION         CLNEVENTS.SEND_DIVISION%type;
    L_SEND_POST             CLNEVENTS.SEND_POST%type;
    L_SEND_PERFORM          CLNEVENTS.SEND_PERFORM%type;
    L_SEND_PERSON           CLNEVENTS.SEND_PERSON%type;
    L_TO_RELEASE_RN         CLNEVENTS_EXT.SOFTRELEASE%type;
    L_TO_BUILD_RN           CLNEVENTS_EXT.SOFTBUILD%type;

  begin
    /* считывание записи */
    P_CLNEVENTS_EXISTS(PKG_SESSION.GET_COMPANY, NRN, L_CRN);
    select *
      into L_EVENT
      from CLNEVENTS
     where COMPANY = PKG_SESSION.GET_COMPANY
       and RN = NRN;

    -- Определим маршрут, соответствующий событию.
    begin
      select RN
        into L_ROUTE
        from EVROUTES
       where EVENT_TYPE = L_EVENT.EVENT_TYPE
         and COMPANY = PKG_SESSION.GET_COMPANY;
    exception
      when NO_DATA_FOUND then
        L_ROUTE := null;
    end;

    /* разрешение ссылок */

    CLAIM_JOINS(SEVENT_TYPE    => SEVENT_TYPE,
                SEVENT_STAT    => SEVENT_STAT,
                SSEND_CLIENT   => SSEND_CLIENT,
                SSEND_DIVISION => SSEND_DIVISION,
                SSEND_POST     => SSEND_POST,
                SSEND_PERFORM  => SSEND_PERFORM,
                SSEND_PERSON   => SSEND_PERSON,
                SINIT_PERSON   => null,
                SCURREL        => null,
                SCURBLD        => null,
                SREL           => SREL_TO,
                SBLD           => SBUILD_TO,

                NEVENT_TYPE    => L_EVENT_TYPE,
                NEVENT_STAT    => L_EVENT_STATUS,
                NSEND_CLIENT   => L_SEND_CLIENT,
                NSEND_DIVISION => L_SEND_DIVISION,
                NSEND_POST     => L_SEND_POST,
                NSEND_PERFORM  => L_SEND_PERFORM,
                NSEND_PERSON   => L_SEND_PERSON,
                NINIT_PERSON   => TMP_NUM,
                NCURREL        => TMP_NUM,
                NCURBLD        => TMP_NUM,
                NREL           => L_TO_RELEASE_RN,
                NBLD           => L_TO_BUILD_RN);

    if L_ROUTE is not null then
      -- 2 - Проверка полномочий на выполнение переходов в следующие точки маршрута;
      P_EVRTPTEXEC_CHECK_RIGHTS(PKG_SESSION.GET_COMPANY, NRN, null, 2);

      -- Пробуем определить точку маршрута,
      -- соответствующую текущему статусу события.
      begin
        select RN
          into L_ROUTE_POINT
          from EVRTPOINTS
         where PRN = L_ROUTE
           and EVENT_STATUS = L_EVENT.EVENT_STAT
           and COMPANY = PKG_SESSION.GET_COMPANY;
      exception
        when NO_DATA_FOUND then
          P_EXCEPTION(0,
                      'Точка маршрута, соответствующая текущему статусу события, не определена. Возможно, маршрутная карта события была модифицирована. Обратитесь к Администратору системы.');
        when others then
          raise;
      end;

      -- Пробуем определить точку маршрута,
      -- соответствующую новому статусу события.
      begin
        select RN
          into L_NEXT_POINT
          from EVRTPOINTS
         where PRN = L_ROUTE
           and EVENT_STATUS = L_EVENT_STATUS
           and COMPANY = PKG_SESSION.GET_COMPANY;
      exception
        when NO_DATA_FOUND then
          P_EXCEPTION(0,
                      'Точка маршрута, соответствующая новому статусу события, не определена. Возможно, маршрутная карта события была модифицирована. Обратитесь к Администратору системы.');
        when others then
          raise;
      end;

      -- Посмотрим, является ли переход условным или нет.
      begin
        select PASS_COND_PROC,
               PASS_ERROR_MSG
          into L_PASS_COND_PROC,
               L_PASS_ERROR_MSG
          from EVRTPTPASS
         where PRN = L_ROUTE_POINT
           and NEXT_POINT = L_NEXT_POINT;
      exception
        when NO_DATA_FOUND then
          P_EXCEPTION(0,
                      'Переход в точку маршрута, соответствующую новому статусу события, не определен. Возможно, маршрутная карта события была модифицирована. Обратитесь к Администратору системы.');
        when others then
          raise;
      end;
      if L_PASS_COND_PROC is not null then
        begin
          select BLOCKTEXT
            into L_SPASS_COND_PROC
            from USERPROCS
           where RN = L_PASS_COND_PROC;
          if L_SPASS_COND_PROC is null then
            P_EXCEPTION(0,
                        'Определена пустая процедура проверки выполнения условия перехода.');
          end if;
        exception
          when NO_DATA_FOUND then
            P_EXCEPTION(0,
                        'Процедура проверки выполнения условия перехода (RN: %S) не найдена.',
                        NVL(TO_CHAR(L_PASS_COND_PROC), '<null>'));
          when others then
            raise;
        end;
        L_SPASS_COND_PROC := replace(L_SPASS_COND_PROC, CHR(13) || CHR(10), ' ');
        L_SPASS_COND_PROC := replace(L_SPASS_COND_PROC, CHR(9), ' ');
        P_CLNEVENTS_EXEC_COND_PROC(PKG_SESSION.GET_COMPANY, L_CRN, NRN, L_SPASS_COND_PROC, L_PASS_COND_PROC_RESULT);
        if L_PASS_COND_PROC_RESULT = 0 then
          if L_PASS_ERROR_MSG is null then
            P_EXCEPTION(0,
                        'Невозможно выполнить переход из текущей точки маршрута, так как не выполняется условие перехода.');
          else
            P_EXCEPTION(0, L_PASS_ERROR_MSG);
          end if;
        end if;
      end if;
    end if;

    /* фиксация начала выполнения действия */
    PKG_ENV.PROLOGUE(PKG_SESSION.GET_COMPANY, null, L_CRN, 'ClientEvents', 'CLAIM_CHANGE_STATE', 'CLNEVENTS', NRN);

    -- Если поле организации исполнителя не определено,
    -- попробуем направить исполнителю по умолчанию.
    if L_SEND_CLIENT is null and L_SEND_DIVISION is null and L_SEND_POST is null and L_SEND_PERFORM is null and
       L_SEND_PERSON is null then
      FIND_EVRTPOINTS_DEFAULT_EXEC(PKG_SESSION.GET_COMPANY,
                                   L_EVENT.INIT_PERSON,
                                   NVL(L_EVENT.CLIENT_CLIENT,
                                       F_CLNCLIENT_BY_PERSON(PKG_SESSION.GET_COMPANY, L_EVENT.CLIENT_PERSON)),
                                   L_EVENT_TYPE,
                                   L_EVENT_STATUS,
                                   L_SEND_CLIENT,
                                   L_SEND_DIVISION,
                                   L_SEND_POST,
                                   L_SEND_PERFORM,
                                   L_SEND_PERSON);

    end if;
    P_CLNEVENTS_BASE_UPDATE(PKG_SESSION.GET_COMPANY,
                            NRN,
                            'CLNEVENTS_CHANGE_STATE',
                            L_EVENT_STATUS,
                            L_EVENT.CLIENT_CLIENT,
                            L_EVENT.CLIENT_PERSON,
                            L_SEND_CLIENT,
                            L_SEND_DIVISION,
                            L_SEND_POST,
                            L_SEND_PERFORM,
                            L_SEND_PERSON,
                            L_EVENT.EVENT_DESCR);
    SET_LAST_HIST_TIME;

    begin
      if NPRIORITY != L_EVENT.PRIORITY then
        P_CLNEVENTS_SET_PRIORITY(PKG_SESSION.GET_COMPANY, NRN, NPRIORITY);
      end if;
      if (L_TO_RELEASE_RN is not null) or (L_TO_BUILD_RN is not null) then
        select *
          into L_EVENT_EXT
          from CLNEVENTS_EXT
         where PRN = NRN;
        update CLNEVENTS_EXT T
           set T.SOFTRELEASE = NVL(L_TO_RELEASE_RN, L_EVENT_EXT.SOFTRELEASE),
               T.SOFTBUILD   = NVL(L_TO_BUILD_RN, L_EVENT_EXT.SOFTBUILD)
         where T.PRN = NRN;
      end if;
    exception
      when others then
        PKG_ENV.CLEAR(PKG_SESSION.GET_COMPANY, null, L_CRN, 'ClientEvents', 'CLAIM_CHANGE_STATE', 'CLNEVENTS', NRN);
        raise;
    end;
    /* Примечание при переходе*/
    if SNOTE is not null then
      WAIT;
      P_CLNEVNOTES_INSERT(NCOMPANY     => PKG_SESSION.GET_COMPANY,
                          NPRN         => NRN,
                          SCLIENT      => null,
                          SAUTHID      => null,
                          SNOTE_HEADER => NVL(SNOTE_HEADER, C_DEFAULT_NOTE_HEADER),
                          SNOTE        => SNOTE,
                          NRN          => TMP_NUM);
      SET_LAST_HIST_TIME;
    end if;

    /* уведомление о выполнении перехода в данную точку */
    /* тип активизации (после перехода в данную точку) - 3 */
    begin
      P_EVRTPTNOT_NOTIFY(PKG_SESSION.GET_COMPANY, NRN, 3);
    exception
      when others then
        PKG_ENV.CLEAR(PKG_SESSION.GET_COMPANY, null, L_CRN, 'ClientEvents', 'CLAIM_CHANGE_STATE', 'CLNEVENTS', NRN);
        raise;
    end;

    /* фиксация окончания выполнения действия */
    PKG_ENV.EPILOGUE(PKG_SESSION.GET_COMPANY, null, L_CRN, 'ClientEvents', 'CLAIM_CHANGE_STATE', 'CLNEVENTS', NRN);
  end CLAIM_CHANGE_STATE;

  procedure CLAIM_DO_SEND
  (
    NRN            in number,
    SEVENT_TYPE    in varchar2,
    SEVENT_STAT    in varchar2,
    SSEND_CLIENT   in varchar2,
    SSEND_DIVISION in varchar2,
    SSEND_POST     in varchar2,
    SSEND_PERFORM  in varchar2,
    SSEND_PERSON   in varchar2,
    SNOTE_HEADER   in varchar2,
    SNOTE          in varchar2
  ) is
    L_CRN           ACATALOG.RN%type;
    L_EVENT         CLNEVENTS%rowtype;
    L_EVENT_STATUS  CLNEVENTS.EVENT_STAT%type;
    L_SEND_CLIENT   CLNEVENTS.SEND_CLIENT%type;
    L_SEND_DIVISION CLNEVENTS.SEND_DIVISION%type;
    L_SEND_POST     CLNEVENTS.SEND_POST%type;
    L_SEND_PERFORM  CLNEVENTS.SEND_PERFORM%type;
    L_SEND_PERSON   CLNEVENTS.SEND_PERSON%type;
  begin
    /* считывание записи */
    P_CLNEVENTS_EXISTS(PKG_SESSION.GET_COMPANY, NRN, L_CRN);
    select *
      into L_EVENT
      from CLNEVENTS
     where COMPANY = PKG_SESSION.GET_COMPANY
       and RN = NRN;
    /* разрешение ссылок */
    CLAIM_JOINS(SEVENT_TYPE    => SEVENT_TYPE,
                SEVENT_STAT    => SEVENT_STAT,
                SSEND_CLIENT   => SSEND_CLIENT,
                SSEND_DIVISION => SSEND_DIVISION,
                SSEND_POST     => SSEND_POST,
                SSEND_PERFORM  => SSEND_PERFORM,
                SSEND_PERSON   => SSEND_PERSON,
                SINIT_PERSON   => null,
                SCURREL        => null,
                SCURBLD        => null,
                SREL           => null,
                SBLD           => null,

                NEVENT_TYPE    => TMP_NUM,
                NEVENT_STAT    => L_EVENT_STATUS,
                NSEND_CLIENT   => L_SEND_CLIENT,
                NSEND_DIVISION => L_SEND_DIVISION,
                NSEND_POST     => L_SEND_POST,
                NSEND_PERFORM  => L_SEND_PERFORM,
                NSEND_PERSON   => L_SEND_PERSON,
                NINIT_PERSON   => TMP_NUM,
                NCURREL        => TMP_NUM,
                NCURBLD        => TMP_NUM,
                NREL           => TMP_NUM,
                NBLD           => TMP_NUM);
    -- 3 - Проверка полномочий на выполнение переадресации в точке маршрута;
    P_EVRTPTEXEC_CHECK_RIGHTS(PKG_SESSION.GET_COMPANY, NRN, null, 3);
    /* фиксация начала выполнения действия */
    PKG_ENV.PROLOGUE(PKG_SESSION.GET_COMPANY, null, L_CRN, 'ClientEvents', 'CLAIM_DO_SEND', 'CLNEVENTS', NRN);

    P_CLNEVENTS_BASE_UPDATE(PKG_SESSION.GET_COMPANY,
                            NRN,
                            'CLNEVENTS_DO_SEND',
                            L_EVENT_STATUS,
                            L_EVENT.CLIENT_CLIENT,
                            L_EVENT.CLIENT_PERSON,
                            L_SEND_CLIENT,
                            L_SEND_DIVISION,
                            L_SEND_POST,
                            L_SEND_PERFORM,
                            L_SEND_PERSON,
                            L_EVENT.EVENT_DESCR);
    SET_LAST_HIST_TIME;
    /* уведомление о выполнении переадресации */
    /* тип активизации (после переадресации) - 2 */
    begin
      P_EVRTPTNOT_NOTIFY(PKG_SESSION.GET_COMPANY, NRN, 2);
    exception
      when others then
        PKG_ENV.CLEAR(PKG_SESSION.GET_COMPANY, null, L_CRN, 'ClientEvents', 'CLAIM_DO_SEND', 'CLNEVENTS', NRN);
        raise;
    end;
    PKG_ENV.EPILOGUE(PKG_SESSION.GET_COMPANY, null, L_CRN, 'ClientEvents', 'CLAIM_DO_SEND', 'CLNEVENTS', NRN);
    WAIT;
    if SNOTE is not null then
      P_CLNEVNOTES_INSERT(NCOMPANY     => PKG_SESSION.GET_COMPANY,
                          NPRN         => NRN,
                          SCLIENT      => null,
                          SAUTHID      => null,
                          SNOTE_HEADER => NVL(SNOTE_HEADER, C_DEFAULT_NOTE_HEADER),
                          SNOTE        => SNOTE,
                          NRN          => TMP_NUM);
    end if;
    SET_LAST_HIST_TIME;
  end;

  procedure CLAIM_RETURN
  (
    NRN          in number,
    SNOTE_HEADER in varchar2,
    SNOTE        in varchar2
  ) is
    L_RETPOINT  PKG_STD.TREF;
    L_COMMENTRY PKG_STD.TLSTRING;
    L_HIST      CLNEVNHIST%rowtype;
  begin
    --  P_CLNEVENTS_RETURN
    FIND_CLNEVENTS_RETPOINT(PKG_SESSION.GET_COMPANY, NRN, L_RETPOINT, L_COMMENTRY);

    -- Получим запись истории, соответствующую предпоследнему изменению статус события.
    select *
      into L_HIST
      from CLNEVNHIST
     where RN = L_RETPOINT;
    /* фиксация начала выполнения действия */
    PKG_ENV.PROLOGUE(PKG_SESSION.GET_COMPANY, null, L_HIST.CRN, 'ClientEvents', 'CLAIM_RETURN', 'CLNEVENTS', NRN);
    /* обновим событие */
    P_CLNEVENTS_BASE_UPDATE(PKG_SESSION.GET_COMPANY,
                            NRN,
                            'CLNEVENTS_RETURN',
                            L_HIST.EVENT_STAT,
                            L_HIST.CLIENT_CLIENT,
                            L_HIST.CLIENT_PERSON,
                            L_HIST.SEND_CLIENT,
                            L_HIST.SEND_DIVISION,
                            L_HIST.SEND_POST,
                            L_HIST.SEND_PERFORM,
                            L_HIST.SEND_PERSON,
                            L_HIST.EVENT_DESCR);
    SET_LAST_HIST_TIME;
    -- уведомление о выполнении возврата
    -- тип активизации (после выполнения возврата в точке маршрута) - 4
    begin
      P_EVRTPTNOT_NOTIFY(PKG_SESSION.GET_COMPANY, NRN, 4);
    exception
      when others then
        PKG_ENV.CLEAR(PKG_SESSION.GET_COMPANY, null, L_HIST.CRN, 'ClientEvents', 'CLAIM_RETURN', 'CLNEVENTS', NRN);
        raise;
    end;

    /* фиксация окончания выполнения действия */
    PKG_ENV.EPILOGUE(PKG_SESSION.GET_COMPANY, null, L_HIST.CRN, 'ClientEvents', 'CLAIM_RETURN', 'CLNEVENTS', NRN);
    if SNOTE is not null then
      WAIT;
      P_CLNEVNOTES_INSERT(NCOMPANY     => PKG_SESSION.GET_COMPANY,
                          NPRN         => NRN,
                          SCLIENT      => null,
                          SAUTHID      => null,
                          SNOTE_HEADER => NVL(SNOTE_HEADER, C_DEFAULT_NOTE_HEADER),
                          SNOTE        => SNOTE,
                          NRN          => TMP_NUM);
      SET_LAST_HIST_TIME;
    end if;
  end;

  procedure CLAIM_CLOSE(NRN in number) is
    L_CRN    ACATALOG.RN%type;
    L_EVENTS CLNEVENTS%rowtype;
    L_HIST   CLNEVNHIST%rowtype;
  begin
    P_CLNEVENTS_EXISTS(PKG_SESSION.GET_COMPANY, NRN, L_CRN);
    /* фиксация начала выполнения действия */
    PKG_ENV.PROLOGUE(PKG_SESSION.GET_COMPANY, null, L_CRN, 'ClientEvents', 'CLAIM_CLOSE', 'CLNEVENTS', NRN);
    select *
      into L_EVENTS
      from CLNEVENTS
     where RN = NRN;

    /* событие может быть аннулировано только один раз */
    if L_EVENTS.CLOSED <> 0 then
      P_EXCEPTION(0, 'Cобытие уже аннулировано.');
    end if;

    -- 5 - Проверка полномочий на выполнение аннулирования в точке маршрута;
    P_EVRTPTEXEC_CHECK_RIGHTS(PKG_SESSION.GET_COMPANY, NRN, null, 5);

    update CLNEVENTS
       set CLOSED = 1
     where RN = NRN;

    begin
      select *
        into L_HIST
        from CLNEVNHIST
       where PRN = NRN
         and CHANGE_DATE = L_EVENTS.CHANGE_DATE;
    exception
      when NO_DATA_FOUND then
        P_EXCEPTION(0, 'Запись истории события не найдена.');
    end;

    /* добавить запись в историю события */
    P_CLNEVNHIST_BASE_INSERT(NRN,
                             null,
                             'CLNEVENTS_CLOSE',
                             L_HIST.EVENT_STAT,
                             null,
                             null,
                             L_HIST.CLIENT_CLIENT,
                             L_HIST.CLIENT_PERSON,
                             L_HIST.SEND_CLIENT,
                             L_HIST.SEND_DIVISION,
                             L_HIST.SEND_POST,
                             L_HIST.SEND_PERFORM,
                             L_HIST.SEND_PERSON,
                             L_HIST.EVENT_DESCR,
                             null,
                             TMP_NUM);

    /* фиксация окончания выполнения действия */
    PKG_ENV.EPILOGUE(PKG_SESSION.GET_COMPANY, null, L_CRN, 'ClientEvents', 'CLAIM_CLOSE', 'CLNEVENTS', NRN);
  end CLAIM_CLOSE;

  procedure CLAIM_HELPSIGN_NEED
  (
    NRN     in number,
    NSTATUS in number,
    SNOTE   in varchar2
  ) is
    L_CRN      ACATALOG.RN%type;
    L_HELPSIGN CLNEVENTS_EXT.HELPSIGN%type;
  begin
    P_CLNEVENTS_EXISTS(PKG_SESSION.GET_COMPANY, NRN, L_CRN);
    select E.HELPSIGN
      into L_HELPSIGN
      from CLNEVENTS_EXT E
     where E.PRN = NRN;
    if L_HELPSIGN != NSTATUS then
      /* фиксация начала выполнения действия */
      PKG_ENV.PROLOGUE(PKG_SESSION.GET_COMPANY, null, L_CRN, 'ClientEvents', 'CLAIM_HELPSIGN_NEED', 'CLNEVENTS', NRN);

      if (NSTATUS between 0 and 5) then
        if (L_HELPSIGN < 21) then
          update CLNEVENTS_EXT
             set HELPSIGN = NSTATUS
           where PRN = NRN;
          if SNOTE is not null then
            P_CLNEVNOTES_INSERT(NCOMPANY     => PKG_SESSION.GET_COMPANY,
                                NPRN         => NRN,
                                SCLIENT      => null,
                                SAUTHID      => null,
                                SNOTE_HEADER => C_DDOC_NOTE_HEADER,
                                SNOTE        => SNOTE,
                                NRN          => TMP_NUM);
          end if;
        else
          P_EXCEPTION(0,
                      'Для рекламации уже задан статус Help. Нельзя изменить его необходимость');
        end if;
      else
        P_EXCEPTION(0, 'Невозможно изменить статус Help');
      end if;

      /* фиксация окончания выполнения действия */
      PKG_ENV.EPILOGUE(PKG_SESSION.GET_COMPANY, null, L_CRN, 'ClientEvents', 'CLAIM_HELPSIGN_NEED', 'CLNEVENTS', NRN);
    end if;
  end;

  procedure CLAIM_HELPSIGN_STAT
  (
    NRN     in number,
    NSTATUS in number
  ) is
    L_CRN      ACATALOG.RN%type;
    L_HELPSIGN CLNEVENTS_EXT.HELPSIGN%type;
  begin
    P_CLNEVENTS_EXISTS(PKG_SESSION.GET_COMPANY, NRN, L_CRN);
    select E.HELPSIGN
      into L_HELPSIGN
      from CLNEVENTS_EXT E
     where E.PRN = NRN;
    if L_HELPSIGN != NSTATUS then
      /* фиксация начала выполнения действия */
      PKG_ENV.PROLOGUE(PKG_SESSION.GET_COMPANY, null, L_CRN, 'ClientEvents', 'CLAIM_HELPSIGN_STAT', 'CLNEVENTS', NRN);

      if (NSTATUS between 21 and 25) then
        if (L_HELPSIGN in (2, 3)) or (L_HELPSIGN between 21 and 25) then
          update CLNEVENTS_EXT
             set HELPSIGN = NSTATUS
           where PRN = NRN;
        else
          P_EXCEPTION(0,
                      'Нельзя указывать статус Help при текущем значении его необходимости');
        end if;
      else
        P_EXCEPTION(0, 'Невозможно изменить необходимость Help');
      end if;

      /* фиксация окончания выполнения действия */
      PKG_ENV.EPILOGUE(PKG_SESSION.GET_COMPANY, null, L_CRN, 'ClientEvents', 'CLAIM_HELPSIGN_STAT', 'CLNEVENTS', NRN);
    end if;
  end;

  procedure CLAIM_ADD_LINKDOC(NCLAIM_RN in number,
                              --    NLINKDOC_BUFFER in number,
                              BTEMPLATE     in blob,
                              SLINKDOC_TYPE in varchar2,
                              SLINKDOC_PATH in varchar2) is
    L_DOCNUMB FILELINKS.CODE%type;
    L_RN      FILELINKS.RN%type;
    cursor LC_DOC is
      select FL.RN
        from FILELINKS      FL,
             FILELINKSUNITS FUL
       where FUL.FILELINKS_PRN = FL.RN
         and FUL.UNITCODE = 'ClientEvents'
         and FUL.TABLE_PRN = NCLAIM_RN
         and FL.FILE_PATH = SLINKDOC_PATH;
  begin
    open LC_DOC;
    fetch LC_DOC
      into L_RN;
    close LC_DOC;
    if L_RN is null then
      L_DOCNUMB := GET_FILELINKS_NEXTNUMB(PKG_SESSION.GET_COMPANY, '');
      P_FILELINKS_INSERT_EX(NCOMPANY   => PKG_SESSION.GET_COMPANY,
                            NCRN       => C_DEFAULT_LINKDOC_CATALOG,
                            SCODE      => L_DOCNUMB,
                            SFILE_TYPE => NVL(SLINKDOC_TYPE, C_DEFAULT_LINKDOC_TYPE),
                            SNOTE      => null,
                            SFILE_PATH => SLINKDOC_PATH,
                            BTEMPLATE  => BTEMPLATE,
                            CTEMPLATE  => null,
                            --                          NTEMPLATEID => NLINKDOC_BUFFER,
                            SUNITCODE  => C_EVENTS_UNITCODE,
                            NTABLE_PRN => NCLAIM_RN,
                            NRN        => L_RN);
    else
      P_FILELINKS_MODIFY(NCOMPANY   => PKG_SESSION.GET_COMPANY,
                         NACTION    => 0,
                         NRN        => L_RN,
                         BDATA      => null,
                         CDATA      => null,
                         SFILE_PATH => SLINKDOC_PATH,
                         DROLLHIST  => null);
      SET_LAST_HIST_TIME;
      WAIT;
      P_FILELINKS_MODIFY(NCOMPANY   => PKG_SESSION.GET_COMPANY,
                         NACTION    => 2,
                         NRN        => L_RN,
                         BDATA      => BTEMPLATE,
                         CDATA      => null,
                         SFILE_PATH => SLINKDOC_PATH,
                         DROLLHIST  => null);
      SET_LAST_HIST_TIME;
      WAIT;
      P_FILELINKS_MODIFY(NCOMPANY   => PKG_SESSION.GET_COMPANY,
                         NACTION    => 1,
                         NRN        => L_RN,
                         BDATA      => null,
                         CDATA      => null,
                         SFILE_PATH => SLINKDOC_PATH,
                         DROLLHIST  => null);

    end if;
  end CLAIM_ADD_LINKDOC;

  procedure PREPARE_HISTORY_TO_WEB
  (
    NEVENT      in number,
    NHAS_DOCUMS out number
  ) is
    MESSAGE_CLASS_NONE  constant varchar2(60) := 'p_evnh_wo_mess';
    MESSAGE_CLASS_EVENT constant varchar2(60) := 'p_evnh_e_mess';
    MESSAGE_CLASS_NOTE  constant varchar2(60) := 'p_evnh_n_mess';

    MESSAGE2_CLASS_WHEN   constant varchar2(60) := 'p_evnh2_when';
    MESSAGE2_CLASS_WHO    constant varchar2(60) := 'p_evnh2_who';
    MESSAGE2_CLASS_WHOM   constant varchar2(60) := 'p_evnh2_whom';
    MESSAGE2_CLASS_STATE  constant varchar2(60) := 'p_evnh2_state';
    MESSAGE2_CLASS_ACTION constant varchar2(60) := 'p_evnh2_act';

    ACTION_INSERT_TEXT       constant varchar2(240) := 'добавил(а) рекламацию';
    ACTION_UPDATE_TEXT       constant varchar2(240) := 'исправил(а) текст рекламации';
    ACTION_DO_SEND_TEXT      constant varchar2(240) := 'перенаправил(а) рекламацию исполнителю';
    ACTION_RETURN_TEXT       constant varchar2(240) := 'вернул(а) рекламацию в состояние';
    ACTION_CLOSE_TEXT        constant varchar2(240) := 'аннулировал(а) рекламацию';
    ACTION_CHANGE_STATE_TEXT constant varchar2(240) := 'перевел(а) рекламацию в состояние';
    ACTION_CLNEVNOTES_TEXT   constant varchar2(240) := 'прокомментировал(а) рекламацию';

    MAX_SHIFT_BETWEEN_NOTE constant number := 1 / 24 / 60 * 5; -- 5 min

    cursor LC_HISTORY is
      select 0 as NFLAG,
             H.DCHANGE_DATE,
             H.SAUTHNAME,
             H.SACTION_CODE,
             H.SEVENT_TYPE_NAME,
             H.SSEND,
             substr(replace(htf.escape_sc(H.STEXT), CHR(10), '<br/>'),1,4000) as STEXT
        from UDO_V_CLAIM_HIST H
       where H.NPRN = NEVENT
       order by DCHANGE_DATE asc;
    type T_HIST is table of LC_HISTORY%rowtype index by binary_integer;
    LT_HISTORY    T_HIST;
    L_CNT         binary_integer;
    LR_HISTORY    UDO_CLAIM_HIST_APEX%rowtype;
    LR_HISTORY_E  UDO_CLAIM_HIST_APEX%rowtype;
    J             binary_integer;
    LAST_EVN_TEXT varchar2(4000);
    function GET_SPAN
    (
      A_CLASS in varchar2,
      A_TEXT  in varchar2
    ) return varchar2 is
    begin
      return '<span class="' || A_CLASS || '">' || A_TEXT || '</span>';
    end;
  begin
    delete UDO_CLAIM_HIST_APEX
     where EVENT = NEVENT;

    open LC_HISTORY;
    fetch LC_HISTORY bulk collect
      into LT_HISTORY;
    close LC_HISTORY;

    L_CNT              := LT_HISTORY.COUNT;
    LR_HISTORY_E.EVENT := NEVENT;
    for I in 1 .. L_CNT loop
      LR_HISTORY            := LR_HISTORY_E;
      LR_HISTORY.EVENT      := NEVENT;
      LR_HISTORY.SWHEN      := TO_CHAR(LT_HISTORY(I).DCHANGE_DATE, 'DD.MM.YYYY HH24:MI:SS');
      LR_HISTORY.WHO        := LT_HISTORY(I).SAUTHNAME;
      LR_HISTORY.WHOM       := LT_HISTORY(I).SSEND;
      LR_HISTORY.ACTION     := LT_HISTORY(I).SACTION_CODE;
      LR_HISTORY.STATE      := '«' || LT_HISTORY(I).SEVENT_TYPE_NAME || '»';
      LR_HISTORY.SMESSAGE   := LT_HISTORY(I).STEXT;
      LR_HISTORY.SMESSCLASS := MESSAGE_CLASS_NONE;
      LR_HISTORY.NORDER     := I;
      LR_HISTORY.SMESSAGE2  := GET_SPAN(MESSAGE2_CLASS_WHEN, LR_HISTORY.SWHEN) || '&nbsp;' ||
                               GET_SPAN(MESSAGE2_CLASS_WHO, LR_HISTORY.WHO) || '&nbsp;';
      case LT_HISTORY(I).SACTION_CODE
        when 'CLNEVENTS_INSERT' then
          LR_HISTORY.SMESSCLASS := MESSAGE_CLASS_EVENT;
          LT_HISTORY(I).NFLAG := 1;
          LAST_EVN_TEXT := LT_HISTORY(I).STEXT;
          LR_HISTORY.SMESSAGE2 := LR_HISTORY.SMESSAGE2 || GET_SPAN(MESSAGE2_CLASS_ACTION, ACTION_INSERT_TEXT);
        when 'CLNEVENTS_UPDATE' then
          if (LT_HISTORY(I).STEXT is not null) and (LT_HISTORY(I).STEXT != LAST_EVN_TEXT) then
            LR_HISTORY.SMESSCLASS := MESSAGE_CLASS_EVENT;
            LT_HISTORY(I).NFLAG := 1;
            LR_HISTORY.SMESSAGE2 := LR_HISTORY.SMESSAGE2 || GET_SPAN(MESSAGE2_CLASS_ACTION, ACTION_UPDATE_TEXT);
            LAST_EVN_TEXT := LT_HISTORY(I).STEXT;
          else
            LR_HISTORY.NORDER := -1;
          end if;
        when 'CLNEVENTS_DO_SEND' then
          LR_HISTORY.SMESSAGE2 := LR_HISTORY.SMESSAGE2 || GET_SPAN(MESSAGE2_CLASS_ACTION, ACTION_DO_SEND_TEXT) ||
                                  '&nbsp;' || GET_SPAN(MESSAGE2_CLASS_WHOM, LR_HISTORY.WHOM);
        when 'CLNEVENTS_RETURN' then
          LR_HISTORY.SMESSAGE2 := LR_HISTORY.SMESSAGE2 || GET_SPAN(MESSAGE2_CLASS_ACTION, ACTION_RETURN_TEXT) ||
                                  '&nbsp;' || GET_SPAN(MESSAGE2_CLASS_STATE, LR_HISTORY.STATE) || '&nbsp;' ||
                                  GET_SPAN(MESSAGE2_CLASS_ACTION, 'Исполнитель:') || '&nbsp;' ||
                                  GET_SPAN(MESSAGE2_CLASS_WHOM, LR_HISTORY.WHOM);
        when 'CLNEVENTS_CLOSE' then
          LR_HISTORY.SMESSAGE2 := LR_HISTORY.SMESSAGE2 || GET_SPAN(MESSAGE2_CLASS_ACTION, ACTION_CLOSE_TEXT);
        when 'CLNEVENTS_CHANGE_STATE' then
          LR_HISTORY.SMESSAGE2 := LR_HISTORY.SMESSAGE2 || GET_SPAN(MESSAGE2_CLASS_ACTION, ACTION_CHANGE_STATE_TEXT) ||
                                  '&nbsp;' || GET_SPAN(MESSAGE2_CLASS_STATE, LR_HISTORY.STATE) || '&nbsp;' ||
                                  GET_SPAN(MESSAGE2_CLASS_ACTION, 'Исполнитель:') || '&nbsp;' ||
                                  GET_SPAN(MESSAGE2_CLASS_WHOM, LR_HISTORY.WHOM);
        else
          LR_HISTORY.NORDER := -1;
      end case;
      if LR_HISTORY.NORDER > 0 then
        insert into UDO_CLAIM_HIST_APEX
        values LR_HISTORY;
      end if;
    end loop;
    for I in 2 .. L_CNT loop
      if (LT_HISTORY(I).SACTION_CODE = 'CLNEVNOTES_INSERT') and (trim(LT_HISTORY(I).STEXT) is not null) then
        -- проверяем предыдущее
        J := I - 1;
        if (LT_HISTORY(J)
           .SACTION_CODE in ('CLNEVENTS_DO_SEND', 'CLNEVENTS_RETURN', 'CLNEVENTS_CLOSE', 'CLNEVENTS_CHANGE_STATE')) and
           (LT_HISTORY(I).SAUTHNAME = LT_HISTORY(J).SAUTHNAME) and
           ((LT_HISTORY(I).DCHANGE_DATE - LT_HISTORY(J).DCHANGE_DATE) < MAX_SHIFT_BETWEEN_NOTE) and
           (LT_HISTORY(J).NFLAG = 0) then
          LT_HISTORY(J).NFLAG := 1;
          update UDO_CLAIM_HIST_APEX
             set SMESSCLASS = MESSAGE_CLASS_NOTE,
                 SMESSAGE   = LT_HISTORY(I).STEXT
           where EVENT = NEVENT
             and NORDER = J;
          goto NEXTLOOP;
        end if;
        -- проверяем следующее
        J := I + 1;
        if J <= L_CNT then
          if (LT_HISTORY(J)
             .SACTION_CODE in ('CLNEVENTS_DO_SEND', 'CLNEVENTS_RETURN', 'CLNEVENTS_CLOSE', 'CLNEVENTS_CHANGE_STATE')) and
             (LT_HISTORY(I).SAUTHNAME = LT_HISTORY(J).SAUTHNAME) and
             ((LT_HISTORY(J).DCHANGE_DATE - LT_HISTORY(I).DCHANGE_DATE) < MAX_SHIFT_BETWEEN_NOTE) and
             (LT_HISTORY(J).NFLAG = 0) then
            LT_HISTORY(J).NFLAG := 1;
            update UDO_CLAIM_HIST_APEX
               set SMESSCLASS = MESSAGE_CLASS_NOTE,
                   SMESSAGE   = LT_HISTORY(I).STEXT
             where EVENT = NEVENT
               and NORDER = J;
            goto NEXTLOOP;
          end if;
        end if;
        -- добавляем отдельную запись
        LR_HISTORY            := LR_HISTORY_E;
        LR_HISTORY.EVENT      := NEVENT;
        LR_HISTORY.SWHEN      := TO_CHAR(LT_HISTORY(I).DCHANGE_DATE, 'DD.MM.YYYY HH24:MI:SS');
        LR_HISTORY.WHO        := LT_HISTORY(I).SAUTHNAME;
        LR_HISTORY.WHOM       := LT_HISTORY(I).SSEND;
        LR_HISTORY.ACTION     := LT_HISTORY(I).SACTION_CODE;
        LR_HISTORY.STATE      := LT_HISTORY(I).SEVENT_TYPE_NAME;
        LR_HISTORY.SMESSAGE   := LT_HISTORY(I).STEXT;
        LR_HISTORY.SMESSCLASS := MESSAGE_CLASS_NOTE;
        LR_HISTORY.NORDER     := I;
        LR_HISTORY.SMESSAGE2  := GET_SPAN(MESSAGE2_CLASS_WHEN, LR_HISTORY.SWHEN) || '&nbsp;' ||
                                 GET_SPAN(MESSAGE2_CLASS_WHO, LR_HISTORY.WHO) || '&nbsp;' ||
                                 GET_SPAN(MESSAGE2_CLASS_ACTION, ACTION_CLNEVNOTES_TEXT);
        insert into UDO_CLAIM_HIST_APEX
        values LR_HISTORY;
      end if;
      <<NEXTLOOP>>
      null;
    end loop;
    select count(*)
      into NHAS_DOCUMS
      from DUAL
     where exists (select *
              from FILELINKSUNITS FUL
             where FUL.TABLE_PRN = NEVENT);

    /*
          LR_HISTORY.ACTION     := LT_HISTORY(I).SACTION_CODE;
          LR_HISTORY.STATE      := LT_HISTORY(I).SEVENT_TYPE_NAME;
          LR_HISTORY.NORDER     := I;
          LR_HISTORY.SMESSAGE   := LT_HISTORY(I).STEXT;
          LR_HISTORY.SMESSCLASS := MESSAGE_CLASS_EVENT;






    CLNEVNOTES_INSERT

    */
  end;

  procedure WEB_LOGIN
  (
    SSESSION            in varchar2,
    SUSERNAME           in varchar2,
    SPASSWORD           in varchar2,
    NCOMPANY            out number,
    NUSER_PESRRN        out number,
    SUSER2              out varchar2,
    NDEPRN              out number,
    SDEPCODE            out varchar2,
    NCOND_IDENT         out number,
    SSELF_PMO_PERFORMER out varchar
  ) is
  begin
    PKG_SESSION.LOGON_WEB(SCONNECT        => SSESSION,
                          SUTILIZER       => SUSERNAME,
                          SPASSWORD       => SPASSWORD,
                          SIMPLEMENTATION => 'Client',
                          SAPPLICATION    => 'Client',
                          SCOMPANY        => 'ORG',
                          SLANGUAGE       => 'RUSSIAN');

    NCOMPANY := PKG_SESSION.GET_COMPANY;

    PKG_SESSION.TIMEOUT_WEB(SCONNECT => SSESSION, NTIMEOUT => 30);

    --:APP_USER_PESRRN := UDO_PKG_CLAIMS.GET_CURRENT_PERSON_RN;
    GET_USERPERFORM(NUSER_PESRRN, SUSER2, NDEPRN, SDEPCODE);
    --:APP_USER2 := UDO_PKG_CLAIMS.GET_CURRENT_PERSON_CODE;
    NCOND_IDENT := GEN_IDENT;

    SSELF_PMO_PERFORMER := GET_SELF_PMO_PERFORMER;
  end;

  procedure GET_AVAIL_ACTIONS
  (
    NRN       in number,
    NUPDATE   out number,
    NDELETE   out number,
    NSTATE    out number,
    NSEND     out number,
    NRETURN   out number,
    NCLOSE    out number,
    NADDNOTE  out number,
    NADDDOCUM out number
  ) is

    BUPDATE   boolean;
    BDELETE   boolean;
    BSTATE    boolean;
    BSEND     boolean;
    BRETURN   boolean;
    BCLOSE    boolean;
    BADDNOTE  boolean;
    BADDDOCUM boolean;

    cursor LC_EVENT is

      select E.CRN,
             PERS.PERS_AUTHID,
             E.EVENT_TYPE,
             E.EVENT_STAT
        from CLNEVENTS  E,
             CLNPERSONS PERS
       where E.RN = NRN
         and E.INIT_PERSON = PERS.RN;

    cursor LC_NEXTPOINTS
    (
      A_EVENT_TYPE   number,
      A_EVENT_STATUS number
    ) is
      select count(*)
        from DUAL
       where exists (select *
                from EVROUTES   R,
                     EVRTPOINTS M
               where M.PRN = R.RN
                 and R.EVENT_TYPE = A_EVENT_TYPE
                 and M.EVENT_STATUS = A_EVENT_STATUS);

    L_EVENT      LC_EVENT%rowtype;
    IS_PMO       boolean;
    IS_INITIATOR boolean;
    HAS_POINTS   number; -- 0/1

    function CHECK_RIGHT
    (
      A_CATALOG    number,
      A_ACTIONCODE varchar2,
      A_UNITCODE   varchar2 default C_EVENTS_UNITCODE
    ) return boolean is
      NRESULT number;
      cursor LC_CHECK_RIGHT is
        select count(*)
          from DUAL
         where exists (select /*+ INDEX(UP I_USERPRIV_CATALOG_AUTHID) INDEX(CP C_UNITPRIV_UK) */
                 null
                  from USERPRIV UP,
                       UNITPRIV CP
                 where UP.RN = CP.PRN
                   and CP.FUNC = A_ACTIONCODE
                   and UP.UNITCODE = A_UNITCODE
                   and UP.CATALOG = A_CATALOG
                   and UP.AUTHID = UTILIZER
                union all
                select /*+ INDEX(UP I_USERPRIV_CATALOG_ROLEID) INDEX(CP C_UNITPRIV_UK) */
                 null
                  from USERPRIV UP,
                       UNITPRIV CP
                 where UP.RN = CP.PRN
                   and CP.FUNC = A_ACTIONCODE
                   and UP.UNITCODE = A_UNITCODE
                   and UP.CATALOG = A_CATALOG
                   and UP.ROLEID in (select /*+ INDEX(UR I_USERROLES_AUTHID_FK) */
                                      UR.ROLEID
                                       from USERROLES UR
                                      where UR.AUTHID = UTILIZER));
    begin
      open LC_CHECK_RIGHT;
      fetch LC_CHECK_RIGHT
        into NRESULT;
      close LC_CHECK_RIGHT;
      return(NRESULT = 1);
    end CHECK_RIGHT;

    function CHECK_RIGHTS_EX(A_ACTION_CODE in number) return boolean is
      NRESULT number;
    begin
      P_EVRTPTEXEC_CHECK_RIGHTS_EX(PKG_SESSION.GET_COMPANY, NRN, null, A_ACTION_CODE, NRESULT);
      return(NRESULT > 0);
    end CHECK_RIGHTS_EX;

    function BOOL_TO_INT(B in boolean) return number is
      NRESULT number;
    begin
      if B then
        NRESULT := 1;
      elsif not B then
        NRESULT := 0;
      else
        NRESULT := null;
      end if;
      return NRESULT;
    end;

  begin
    open LC_EVENT;
    fetch LC_EVENT
      into L_EVENT;
    close LC_EVENT;
    if L_EVENT.CRN is null then
      return;
    end if;
    IS_PMO       := GET_SELF_PMO_PERFORMER is not null;
    IS_INITIATOR := (L_EVENT.PERS_AUTHID = UTILIZER);
    BUPDATE      := CHECK_RIGHT(L_EVENT.CRN, 'CLAIM_UPDATE') and (IS_PMO or IS_INITIATOR);
    BDELETE      := CHECK_RIGHT(L_EVENT.CRN, 'CLAIM_DELETE');
    BSTATE       := CHECK_RIGHT(L_EVENT.CRN, 'CLAIM_CHANGE_STATE') and CHECK_RIGHTS_EX(2);
    BSEND        := CHECK_RIGHT(L_EVENT.CRN, 'CLAIM_DO_SEND') and CHECK_RIGHTS_EX(3);
    BRETURN      := CHECK_RIGHT(L_EVENT.CRN, 'CLAIM_RETURN') and CHECK_RIGHTS_EX(4);
    BCLOSE       := CHECK_RIGHT(L_EVENT.CRN, 'CLAIM_CLOSE') and CHECK_RIGHTS_EX(5);
    BADDNOTE     := CHECK_RIGHT(L_EVENT.CRN, 'CLNEVNOTES_INSERT');
    BADDDOCUM    := CHECK_RIGHT(C_DEFAULT_LINKDOC_CATALOG, 'FILELINKS_INSERT', 'FileLinks');
    if BSTATE then
      open LC_NEXTPOINTS(L_EVENT.EVENT_TYPE, L_EVENT.EVENT_STAT);
      fetch LC_NEXTPOINTS
        into HAS_POINTS;
      close LC_NEXTPOINTS;
      BSTATE := BSTATE and (HAS_POINTS > 0);
    end if;

    if BRETURN then
      begin
        FIND_CLNEVENTS_RETPOINT(PKG_SESSION.GET_COMPANY, NRN => NRN, NPOINT_OUT => TMP_NUM, SCOMMENTRY => TMP_STR);
      exception
        when others then
          BRETURN := false;
      end;
    end if;

    NUPDATE   := BOOL_TO_INT(BUPDATE);
--    NUPDATE   := 1;
    NDELETE   := BOOL_TO_INT(BDELETE);
    NSTATE    := BOOL_TO_INT(BSTATE);
    NSEND     := BOOL_TO_INT(BSEND);
    NRETURN   := BOOL_TO_INT(BRETURN);
    NCLOSE    := BOOL_TO_INT(BCLOSE);
    NADDNOTE  := BOOL_TO_INT(BADDNOTE);
    NADDDOCUM := BOOL_TO_INT(BADDDOCUM);
  end;

  procedure GET_CURRENT_RELEASES
  (
    STAB_R out number,
    STAB_B out number,
    BETA_R out number,
    BETA_B out number
  ) is
    cursor LC_R is
      select RN
        from UDO_SOFTRELEASES
        --where relname != '8.5.6.R3'
       order by BEGDATE desc nulls last;
    cursor LC_B(A_PRN number) is
      select RN
        from UDO_SOFTBUILDS
       where PRN = A_PRN
       order by BUILDATE desc nulls last;
  begin
    open LC_R;
    fetch LC_R
      into BETA_R;
    fetch LC_R
      into STAB_R;
    close LC_R;
    open LC_B(BETA_R);
    fetch LC_B
      into BETA_B;
    close LC_B;
    open LC_B(STAB_R);
    fetch LC_B
      into STAB_B;
    close LC_B;
  end;

end UDO_PKG_CLAIMS;

/
