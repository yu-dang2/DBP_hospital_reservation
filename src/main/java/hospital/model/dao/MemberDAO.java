package hospital.model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
<<<<<<< HEAD
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.util.Date;
import MemberRole;
import MemberStatus;

=======
import java.sql.Timestamp;
import java.util.Date;
import hospital.model.domain.MemberRole; 
import hospital.model.domain.MemberStatus;
>>>>>>> c1f575341f1159b04e8168247d144ee1214c1882
import hospital.model.domain.GeneralMember;
import hospital.model.domain.Member;
import util.JDBCUtil;

/**
 * MEMBER 테이블 접근 클래스 (담당: 김민진)
 * 업무 규칙(비밀번호 해싱, 본인 확인, 중복 결과 해석)은 MemberManager가 담당한다.
 */
public class MemberDAO {
<<<<<<< HEAD

    public static final String MemberStatus.ACTIVE.getCode() = "ACTIVE";
    public static final String WITHDRAWN.getCode() = "WITHDRAWN";

=======
>>>>>>> c1f575341f1159b04e8168247d144ee1214c1882
    private static final String SELECT_MEMBER_COLUMNS =
            "SELECT MEMBER_ID, USER_ID, PASSWORD, NAME, BIRTH, PHONE, EMAIL, ROLE, STATUS, CREATED_AT "
          + "FROM MEMBER ";

    // 로그인 아이디로 회원 조회
<<<<<<< HEAD
    public Member findByUserId(String userId) {
=======
    public Member findByUserId(String userId) throws SQLException {
>>>>>>> c1f575341f1159b04e8168247d144ee1214c1882
        StringBuffer query = new StringBuffer();
        query.append(SELECT_MEMBER_COLUMNS);
        query.append("WHERE USER_ID = ? ");
        return queryOne(query.toString(), new Object[] { userId });
    }

    // 회원 번호로 회원 조회
<<<<<<< HEAD
    public Member findById(Long memberId) {
=======
    public Member findById(Long memberId) throws SQLException {
>>>>>>> c1f575341f1159b04e8168247d144ee1214c1882
        StringBuffer query = new StringBuffer();
        query.append(SELECT_MEMBER_COLUMNS);
        query.append("WHERE MEMBER_ID = ? ");
        return queryOne(query.toString(), new Object[] { memberId });
    }

    // 아이디 중복 확인 (가입 화면 중복 확인, 가입 직전 재검사)
<<<<<<< HEAD
    public boolean existsByUserId(String userId) {
=======
    public boolean existsByUserId(String userId) throws SQLException {
>>>>>>> c1f575341f1159b04e8168247d144ee1214c1882
        StringBuffer query = new StringBuffer();
        query.append("SELECT USER_ID ");
        query.append("FROM MEMBER ");
        query.append("WHERE USER_ID = ? ");

        JDBCUtil jdbcUtil = new JDBCUtil();
        try {
            jdbcUtil.setSqlAndParameters(query.toString(), new Object[] { userId });
            ResultSet rs = jdbcUtil.executeQuery();
<<<<<<< HEAD
            return rs.next();
        } catch (SQLException ex) {
            throw new DaoException("existsByUserId 실패: " + userId, ex);
        } finally {
=======
            
            if (rs == null) {
                throw new SQLException("existsByUserId 실패: " + userId);
            }
            return rs.next();
        }finally {
>>>>>>> c1f575341f1159b04e8168247d144ee1214c1882
            jdbcUtil.close();
        }
    }

    // 신규 회원 등록. MEMBER_ID는 SEQ_MEMBER.NEXTVAL, member.getPassword()는 Manager가 해시한 값이어야 한다.
<<<<<<< HEAD
    public long insert(Member member) {
=======
    public long insert(Member member) throws SQLException {
>>>>>>> c1f575341f1159b04e8168247d144ee1214c1882
        long memberId = nextMemberId();

        StringBuffer query = new StringBuffer();
        query.append("INSERT INTO MEMBER (MEMBER_ID, USER_ID, PASSWORD, NAME, BIRTH, PHONE, EMAIL, ROLE, STATUS, CREATED_AT) ");
        query.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE) ");

        JDBCUtil jdbcUtil = new JDBCUtil();
        try {
            jdbcUtil.setSqlAndParameters(query.toString(), new Object[] {
                    memberId, 
                    member.getUserId(), 
                    member.getPassword(), 
                    member.getName(),
                    toTimestamp(member.getBirth()),
                     member.getPhone(),
                      member.getEmail(), 
<<<<<<< HEAD
                     member.getRole().getcode(),
                    MemberStatus.ACTIVE.getCode() });
            jdbcUtil.executeUpdate();
            jdbcUtil.commit();
        } catch (Exception ex) {
            jdbcUtil.rollback();
            throw new DaoException("insert 실패: " + member.getUserId(), ex);
=======
                     member.getRole().getCode(),
                    MemberStatus.ACTIVE.getCode() });
            jdbcUtil.executeUpdate();
            jdbcUtil.commit();
        }  catch (SQLException ex) {
            jdbcUtil.rollback();
            throw ex;
        } catch (Exception ex) {
            jdbcUtil.rollback();
            throw new SQLException(ex);
>>>>>>> c1f575341f1159b04e8168247d144ee1214c1882
        } finally {
            jdbcUtil.close();
        }

        member.setMemberId(memberId);
        return memberId;
    }

    // 회원 정보 수정. password가 null 또는 빈 값이면 기존 비밀번호를 유지한다.
    // member.getMemberId()는 Manager가 세션의 본인 memberId로 설정해야 한다. 반환값은 갱신 행 수.
<<<<<<< HEAD
    public int update(Member member) {
=======
    public int update(Member member) throws SQLException {
>>>>>>> c1f575341f1159b04e8168247d144ee1214c1882
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
            params = new Object[] { member.getPassword(), member.getName(), toTimestamp(member.getBirth()),
                    member.getPhone(), member.getEmail(), member.getMemberId(), MemberStatus.ACTIVE.getCode() };
        } else {
            params = new Object[] { member.getName(), toTimestamp(member.getBirth()),
                    member.getPhone(), member.getEmail(), member.getMemberId(), MemberStatus.ACTIVE.getCode() };
        }

        JDBCUtil jdbcUtil = new JDBCUtil();
        try {
            jdbcUtil.setSqlAndParameters(query.toString(), params);
            int rows = jdbcUtil.executeUpdate();
            jdbcUtil.commit();
            return rows;
<<<<<<< HEAD
        } catch (Exception ex) {
            jdbcUtil.rollback();
            throw new DaoException("update 실패: memberId=" + member.getMemberId(), ex);
=======
        } catch (SQLException ex) {
            jdbcUtil.rollback();
            throw ex;
        } catch (Exception ex) {
            jdbcUtil.rollback();
            throw new SQLException(ex);
>>>>>>> c1f575341f1159b04e8168247d144ee1214c1882
        } finally {
            jdbcUtil.close();
        }
    }

    // 탈퇴 처리: 행은 삭제하지 않고 STATUS만 변경한다. 반환값은 갱신 행 수.
<<<<<<< HEAD
    public int markWithdrawn(Long memberId) {
=======
    public int markWithdrawn(Long memberId) throws SQLException {
>>>>>>> c1f575341f1159b04e8168247d144ee1214c1882
        StringBuffer query = new StringBuffer();
        query.append("UPDATE MEMBER ");
        query.append("SET STATUS = ? ");
        query.append("WHERE MEMBER_ID = ? AND STATUS = ? ");

        JDBCUtil jdbcUtil = new JDBCUtil();
        try {
            jdbcUtil.setSqlAndParameters(query.toString(),
<<<<<<< HEAD
                    new Object[] { WITHDRAWN.getCode(), memberId, MemberStatus.ACTIVE.getCode() });
            int rows = jdbcUtil.executeUpdate();
            jdbcUtil.commit();
            return rows;
        } catch (Exception ex) {
            jdbcUtil.rollback();
            throw new DaoException("markWithdrawn 실패: memberId=" + memberId, ex);
=======
                    new Object[] { MemberStatus.WITHDRAWN.getCode(), memberId, MemberStatus.ACTIVE.getCode() });
            int rows = jdbcUtil.executeUpdate();
            jdbcUtil.commit();
            return rows;
        } catch (SQLException ex) {
            jdbcUtil.rollback();
            throw ex;
        } catch (Exception ex) {
            jdbcUtil.rollback();
            throw new SQLException(ex);
>>>>>>> c1f575341f1159b04e8168247d144ee1214c1882
        } finally {
            jdbcUtil.close();
        }
    }

    // ---------- private ----------

<<<<<<< HEAD
    private long nextMemberId() {
=======
    private long nextMemberId() throws SQLException {
>>>>>>> c1f575341f1159b04e8168247d144ee1214c1882
        StringBuffer query = new StringBuffer();
        query.append("SELECT SEQ_MEMBER.NEXTVAL AS MEMBER_ID ");
        query.append("FROM DUAL ");

        JDBCUtil jdbcUtil = new JDBCUtil();
        try {
            jdbcUtil.setSqlAndParameters(query.toString(), null);
            ResultSet rs = jdbcUtil.executeQuery();
<<<<<<< HEAD
            rs.next();
            return rs.getLong("MEMBER_ID");
        } catch (SQLException ex) {
            throw new DaoException("SEQ_MEMBER 조회 실패", ex);
=======
            if (rs == null) {
                throw new SQLException("SEQ_MEMBER 조회 실패");
            }
            rs.next();
            return rs.getLong("MEMBER_ID");
>>>>>>> c1f575341f1159b04e8168247d144ee1214c1882
        } finally {
            jdbcUtil.close();
        }
    }

<<<<<<< HEAD
    private Member queryOne(String sql, Object[] params) {
=======
    private Member queryOne(String sql, Object[] params) throws SQLException {
>>>>>>> c1f575341f1159b04e8168247d144ee1214c1882
        JDBCUtil jdbcUtil = new JDBCUtil();
        try {
            jdbcUtil.setSqlAndParameters(sql, params);
            ResultSet rs = jdbcUtil.executeQuery();
<<<<<<< HEAD
            if (rs.next()) {
                return toMember(rs);
            }
        } catch (SQLException ex) {
            throw new DaoException("회원 조회 실패", ex);
        } finally {
            jdbcUtil.close();
        }
        return null;
=======
            if (rs == null) {
                throw new SQLException("회원 조회 실패");
            }
            if (rs.next()) {
                return toMember(rs);
            }
            return null;
        } finally {
            jdbcUtil.close();
        }
>>>>>>> c1f575341f1159b04e8168247d144ee1214c1882
    }

    private Member toMember(ResultSet rs) throws SQLException {
        GeneralMember member = new GeneralMember();
        member.setMemberId(rs.getLong("MEMBER_ID"));
        member.setUserId(rs.getString("USER_ID"));
        member.setPassword(rs.getString("PASSWORD"));
        member.setName(rs.getString("NAME"));
        member.setBirth(rs.getDate("BIRTH"));
        member.setPhone(rs.getString("PHONE"));
        member.setEmail(rs.getString("EMAIL"));
        member.setRole(MemberRole.fromCode(rs.getString("ROLE")));
        member.setStatus(MemberStatus.fromCode(rs.getString("STATUS")));
        member.setCreatedAt(rs.getTimestamp("CREATED_AT"));
        return member;
    }
      // java.util.Date를 DB 전달용 Timestamp로 변환 (null이면 null)
    private Timestamp toTimestamp(Date date) {
        return date == null ? null : new Timestamp(date.getTime());
    }
}
