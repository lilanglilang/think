
/*
 * public void add(int field,int amount):根据给定的日历字段和对应的时间,来对当前的日历进行操作。(根据日历字段,增加或减去)
 * public final void set(int year,int month,int date):设置当前日历的年月日。(直接设置日历值);
 */
public class CalendarDemo {
	public static void main(String[] args) {
		// 获取当前的日历时间
		Calendar c = Calendar.getInstance();

		// 获取月
		int year = c.get(Calendar.YEAR);
		// 获取月
		int month = c.get(Calendar.MONTH);
		// 获取日
		int date = c.get(Calendar.DATE);
		System.out.println(year + "年" + (month + 1) + "月" + date + "日");

		// // 三年前的今天
		// c.add(Calendar.YEAR, -3);
		// // 获取月
		// year = c.get(Calendar.YEAR);
		// // 获取月
		// month = c.get(Calendar.MONTH);
		// // 获取日
		// date = c.get(Calendar.DATE);
		// System.out.println(year + "年" + (month + 1) + "月" + date + "日");

		// 5年后的10天前
		c.add(Calendar.YEAR, 5);
		c.add(Calendar.DATE, -10);
		// 获取月
		year = c.get(Calendar.YEAR);
		// 获取月
		month = c.get(Calendar.MONTH);
		// 获取日
		date = c.get(Calendar.DATE);
		System.out.println(year + "年" + (month + 1) + "月" + date + "日");// 2021年11月30日
		System.out.println("-----------");

		c.set(2011, 11, 11);
		// 获取月
		year = c.get(Calendar.YEAR);
		// 获取月
		month = c.get(Calendar.MONTH);
		// 获取日
		date = c.get(Calendar.DATE);
		System.out.println(year + "年" + (month + 1) + "月" + date + "日");
	}

	public static List<String> getBeenByDays(String start_time, String end_time) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date sdate = null;
		Date eDate = null;
		try {
			sdate = format.parse(start_time);
			eDate = format.parse(end_time);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Calendar c = Calendar.getInstance();
		List<String> list = new ArrayList<String>();
		while (sdate.getTime() <= eDate.getTime()) {
			list.add(format.format(sdate));
			c.setTime(sdate);
			c.add(Calendar.DATE, 1); // 日期加1天
			sdate = c.getTime();
		}
		return list;
	}
}