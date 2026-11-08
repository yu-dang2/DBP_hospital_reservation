package hospital.model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import hospital.model.domain.GeneralMember;
import hospital.model.domain.Member;
import util.JDBCUtil;

/**
 * MEMBER 테이블 접근 클래스 (담당: 김민진)
 * 업무 규칙(비밀번호 해싱, 본인 확인, 중복 결과 해석)은 MemberManager가 담당한다.
 */
public class MemberDAO {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_WITHDRAWN = "WITHDRAWN";

    private static final String SELECT_MEMBER_COLUMNS =
            "SELECT MEMBER_ID, USER_ID, PASSWORD, NAME, BIRTH, PHONE, EMAIL, ROLE, STATUS, CREATED_AT "
          + "FROM MEMBER ";

    // 로그인 아이디로 회원 조회
    public Member findByUserId(String userId) {
        StringBuffer query = new StringBuffer();
        query.append(SELECT_MEMBER_COLUMNS);
        query.append("WHERE USER_ID = ? ");
        return queryOne(query.toString(), new Object[] { userId });
    }

    // 회원 번호로 회원 조회
    public Member findById(Long memberId) {
        StringBuffer query = new StringBuffer();
        query.append(SELECT_MEMBER_COLUMNS);
        query.append("WHERE MEMBER_ID = ? ");
        return queryOne(query.toString(), new Object[] { memberId });
    }

    // 아이디 중복 확인 (가입 화면 중복 확인, 가입 직전 재검사)
    public boolean existsByUserId(String userId) {
        StringBuffer query = new StringBuffer();
        query.append("SELECT USER_ID ");
        query.append("FROM MEMBER ");
        query.append("WHERE USER_ID = ? ");

        JDBCUtil jdbcUtil = new JDBCUtil();
        try {
            jdbcUtil.setSqlAndParameters(query.toString(), new Object[] { userId });
            ResultSet rs = jdbcUtil.executeQuery();
            return rs.next();
        } catch (SQLException ex) {
            throw new DaoException("existsByUserId 실패: " + userId, ex);
        } finally {
            jdbcUtil.close();
        }
    }

    // 신규 회원 등록. MEMBER_ID는 SEQ_MEMBER_ID.NEXTVAL, member.getPassword()는 Manager가 해시한 값이어야 한다.
    public long insert(Member member) {
        long memberId = nextMemberId();

        StringBuffer query = new StringBuffer();
        query.append("INSERT INTO MEMBER (MEMBER_ID, USER_ID, PASSWORD, NAME, BIRTH, PHONE, EMAIL, ROLE, STATUS, CREATED_AT) ");
        query.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE) ");

        JDBCUtil jdbcUtil = new JDBCUtil();
        try {
            jdbcUtil.setSqlAndParameters(query.toString(), new Object[] {
                    memberId, member.getUserId(), member.getPassword(), member.getName(),
                    member.getBirth(), member.getPhone(), member.getEmail(), member.getRole(),
                    STATUS_ACTIVE });
            jdbcUtil.executeUpdate();
            jdbcUtil.commit();
        } catch (SQLException ex) {
            jdbcUtil.rollback();
            throw new DaoException("insert 실패: " + member.getUserId(), ex);
        } finally {
            jdbcUtil.close();
        }

        member.setMemberId(memberId);
        return memberId;
    }

    // 회원 정보 수정. password가 null 또는 빈 값이면 기존 비밀번호를 유지한다.
    // member.getMemberId()는 Manager가 세션의 본인 memberId로 설정해야 한다. 반환값은 갱신 행 수.
    public int update(Member member) {
        boolean changePassword = member.getPassword() != null && !member.getPassword().isEmpty();

        StringBuffer query = new StringBuffer();
        query.append("UPDATE MEMBER ");
        query.append("SET ");
        if (changePassword) {
            query.append("PASSWORD = ?, ");
        }
        query.append("NAME = ?, BIRTH = ?, PHONE = ?, EMAIL = ? ");
        query.append("WHERE MEMBER_ID = ? AND STATUS = ? ");

        Object[] params;
        if (changePassword) {
            params = new Object[] { member.getPassword(), member.getName(), member.getBirth(),
                    member.getPhone(), member.getEmail(), member.getMemberId(), STATUS_ACTIVE };
        } else {
            params = new Object[] { member.getName(), member.getBirth(),
                    member.getPhone(), member.getEmail(), member.getMemberId(), STATUS_ACTIVE };
        }

        JDBCUtil jdbcUtil = new JDBCUtil();
        try {
            jdbcUtil.setSqlAndParameters(query.toString(), params);
            int rows = jdbcUtil.executeUpdate();
            jdbcUtil.commit();
            return rows;
        } catch (SQLException ex) {
            jdbcUtil.rollback();
            throw new DaoException("update 실패: memberId=" + member.getMemberId(), ex);
        } finally {
            jdbcUtil.close();
        }
    }

    // 탈퇴 처리: 행은 삭제하지 않고 STATUS만 변경한다. 반환값은 갱신 행 수.
    public int markWithdrawn(Long memberId) {
        StringBuffer query = new StringBuffer();
        query.append("UPDATE MEMBER ");
        query.append("SET STATUS = ? ");
        query.append("WHERE MEMBER_ID = ? AND STATUS = ? ");

        JDBCUtil jdbcUtil = new JDBCUtil();
        try {
            jdbcUtil.setSqlAndParameters(query.toString(),
                    new Object[] { STATUS_WITHDRAWN, memberId, STATUS_ACTIVE });
            int rows = jdbcUtil.executeUpdate();
            jdbcUtil.commit();
            return rows;
        } catch (SQLException ex) {
            jdbcUtil.rollback();
            throw new DaoException("markWithdrawn 실패: memberId=" + memberId, ex);
        } finally {
            jdbcUtil.close();
        }
    }

    // ---------- private ----------

    private long nextMemberId() {
        StringBuffer query = new StringBuffer();
        query.append("SELECT SEQ_MEMBER_ID.NEXTVAL AS MEMBER_ID ");
        query.append("FROM DUAL ");

        JDBCUtil jdbcUtil = new JDBCUtil();
        try {
            jdbcUtil.setSqlAndParameters(query.toString(), null);
            ResultSet rs = jdbcUtil.executeQuery();
            rs.next();
            return rs.getLong("MEMBER_ID");
        } catch (SQLException ex) {
            throw new DaoException("SEQ_MEMBER_ID 조회 실패", ex);
        } finally {
            jdbcUtil.close();
        }
    }

    private Member queryOne(String sql, Object[] params) {
        JDBCUtil jdbcUtil = new JDBCUtil();
        try {
            jdbcUtil.setSqlAndParameters(sql, params);
            ResultSet rs = jdbcUtil.executeQuery();
            if (rs.next()) {
                return toMember(rs);
            }
        } catch (SQLException ex) {
            throw new DaoException("회원 조회 실패", ex);
        } finally {
            jdbcUtil.close();
        }
        return null;
    }

    private Member toMember(ResultSet rs) throws SQLException {
        GeneralMember member = new GeneralMember();
        member.setMemberId(rs.getLong("MEMBER_ID"));
        member.setUserId(rs.getString("USER_ID"));
        member.setPassword(rs.getString("PASSWORD"));
        member.setName(rs.getString("NAME"));
        member.setBirth(rs.getObject("BIRTH", LocalDate.class));
        member.setPhone(rs.getString("PHONE"));
        member.setEmail(rs.getString("EMAIL"));
        member.setRole(rs.getString("ROLE"));
        member.setStatus(rs.getString("STATUS"));
        member.setCreatedAt(rs.getObject("CREATED_AT", LocalDateTime.class));
        return member;
    }
}
