"""
Subject analytics module.
Analyzes subject difficulty, pass/fail ratios, and score distributions.
No database access - pure data processing.
"""

from typing import List, Dict, Any, Optional
from statistics import mean, median, stdev, variance
from collections import defaultdict
import math


class SubjectAnalytics:
    """Analyzes subject-level performance data."""
    
    @staticmethod
    def analyze_subject_difficulty(subject_marks: List[Dict[str, Any]], 
                                   subject_name: str) -> Dict[str, Any]:
        """
        Analyze subject difficulty based on student performance.
        
        Args:
            subject_marks: List of marks for the subject
            subject_name: Name of the subject
            
        Returns:
            Subject difficulty analysis
        """
        if not subject_marks:
            return {
                'subject_name': subject_name,
                'difficulty': 'Unknown',
                'note': 'No data available'
            }
        
        marks_values = [float(m['marks_obtained']) for m in subject_marks]
        
        avg_marks = mean(marks_values)
        pass_count = sum(1 for m in marks_values if m >= 40)
        pass_rate = (pass_count / len(marks_values) * 100) if marks_values else 0
        
        # Difficulty rating based on average and pass rate
        if avg_marks >= 75 and pass_rate >= 90:
            difficulty = "Easy"
            difficulty_score = 1
        elif avg_marks >= 65 and pass_rate >= 80:
            difficulty = "Moderate"
            difficulty_score = 2
        elif avg_marks >= 55 and pass_rate >= 70:
            difficulty = "Moderate-Hard"
            difficulty_score = 3
        elif avg_marks >= 45 and pass_rate >= 60:
            difficulty = "Hard"
            difficulty_score = 4
        else:
            difficulty = "Very Hard"
            difficulty_score = 5
        
        std_dev = stdev(marks_values) if len(marks_values) > 1 else 0
        
        # Consistency analysis
        if std_dev < 10:
            consistency = "High - Students perform uniformly"
        elif std_dev < 20:
            consistency = "Moderate - Some variation in performance"
        else:
            consistency = "Low - Wide performance gap"
        
        return {
            'subject_name': subject_name,
            'total_students': len(marks_values),
            'difficulty': difficulty,
            'difficulty_score': difficulty_score,
            'average_marks': round(avg_marks, 2),
            'pass_rate': round(pass_rate, 2),
            'fail_rate': round(100 - pass_rate, 2),
            'highest_marks': round(max(marks_values), 2),
            'lowest_marks': round(min(marks_values), 2),
            'median_marks': round(median(marks_values), 2),
            'std_deviation': round(std_dev, 2),
            'performance_consistency': consistency
        }
    
    @staticmethod
    def calculate_pass_fail_ratio(marks_data: List[Dict[str, Any]], 
                                  passing_threshold: float = 40.0) -> Dict[str, Any]:
        """
        Calculate detailed pass/fail ratios.
        
        Args:
            marks_data: List of marks records
            passing_threshold: Passing marks threshold
            
        Returns:
            Pass/fail ratio analysis
        """
        if not marks_data:
            return {
                'total_students': 0,
                'pass_fail_ratio': 'No data'
            }
        
        marks_values = [float(m['marks_obtained']) for m in marks_data]
        
        passed = sum(1 for m in marks_values if m >= passing_threshold)
        failed = len(marks_values) - passed
        
        pass_rate = (passed / len(marks_values) * 100) if marks_values else 0
        fail_rate = 100 - pass_rate
        
        # Grade-wise distribution
        grade_dist = {
            'distinction (75+)': sum(1 for m in marks_values if m >= 75),
            'first_class (60-74)': sum(1 for m in marks_values if 60 <= m < 75),
            'second_class (50-59)': sum(1 for m in marks_values if 50 <= m < 60),
            'pass_class (40-49)': sum(1 for m in marks_values if 40 <= m < 50),
            'fail (<40)': sum(1 for m in marks_values if m < 40)
        }
        
        # Ratio
        ratio = f"{passed}:{failed}" if failed > 0 else f"{passed}:0"
        
        return {
            'total_students': len(marks_values),
            'passed': passed,
            'failed': failed,
            'pass_rate': round(pass_rate, 2),
            'fail_rate': round(fail_rate, 2),
            'pass_fail_ratio': ratio,
            'grade_distribution': grade_dist,
            'health_status': 'Healthy' if pass_rate >= 75 else 'Needs Attention'
        }
    
    @staticmethod
    def analyze_score_distribution(marks_data: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Analyze statistical distribution of scores.
        
        Args:
            marks_data: List of marks records
            
        Returns:
            Score distribution analysis
        """
        if not marks_data:
            return {'distribution': 'No data'}
        
        marks_values = [float(m['marks_obtained']) for m in marks_data]
        
        # Basic statistics
        avg = mean(marks_values)
        med = median(marks_values)
        std = stdev(marks_values) if len(marks_values) > 1 else 0
        var = variance(marks_values) if len(marks_values) > 1 else 0
        
        # Quartiles
        sorted_marks = sorted(marks_values)
        n = len(sorted_marks)
        q1 = sorted_marks[n // 4]
        q2 = med
        q3 = sorted_marks[(3 * n) // 4]
        iqr = q3 - q1
        
        # Skewness (simplified)
        if std > 0:
            skewness = sum((m - avg) ** 3 for m in marks_values) / (len(marks_values) * std ** 3)
            
            if skewness > 0.5:
                skew_interpretation = "Positively skewed - more low scores"
            elif skewness < -0.5:
                skew_interpretation = "Negatively skewed - more high scores"
            else:
                skew_interpretation = "Symmetric - balanced distribution"
        else:
            skewness = 0
            skew_interpretation = "No variation"
        
        # Distribution bins
        bins = {
            '0-20': sum(1 for m in marks_values if 0 <= m < 20),
            '20-40': sum(1 for m in marks_values if 20 <= m < 40),
            '40-60': sum(1 for m in marks_values if 40 <= m < 60),
            '60-80': sum(1 for m in marks_values if 60 <= m < 80),
            '80-100': sum(1 for m in marks_values if 80 <= m <= 100)
        }
        
        return {
            'mean': round(avg, 2),
            'median': round(med, 2),
            'mode': SubjectAnalytics._calculate_mode(marks_values),
            'std_deviation': round(std, 2),
            'variance': round(var, 2),
            'quartiles': {
                'Q1': round(q1, 2),
                'Q2': round(q2, 2),
                'Q3': round(q3, 2),
                'IQR': round(iqr, 2)
            },
            'skewness': round(skewness, 3),
            'skew_interpretation': skew_interpretation,
            'distribution_bins': bins,
            'range': round(max(marks_values) - min(marks_values), 2)
        }
    
    @staticmethod
    def _calculate_mode(values: List[float]) -> float:
        """Calculate mode of marks."""
        from collections import Counter
        # Round to nearest integer for mode calculation
        rounded = [round(v) for v in values]
        if not rounded:
            return 0
        counter = Counter(rounded)
        mode_val = counter.most_common(1)[0][0]
        return float(mode_val)
    
    @staticmethod
    def compare_subjects(subjects_data: Dict[str, List[Dict[str, Any]]]) -> Dict[str, Any]:
        """
        Compare multiple subjects' performance.
        
        Args:
            subjects_data: Dictionary mapping subject_name to marks data
            
        Returns:
            Comparative analysis of subjects
        """
        if not subjects_data:
            return {'comparison': 'No data'}
        
        comparisons = []
        
        for subject_name, marks_data in subjects_data.items():
            if marks_data:
                marks_values = [float(m['marks_obtained']) for m in marks_data]
                pass_rate = (sum(1 for m in marks_values if m >= 40) / len(marks_values) * 100)
                
                comparisons.append({
                    'subject_name': subject_name,
                    'average_marks': round(mean(marks_values), 2),
                    'pass_rate': round(pass_rate, 2),
                    'total_students': len(marks_values),
                    'difficulty_score': SubjectAnalytics._get_difficulty_score(mean(marks_values), pass_rate)
                })
        
        # Sort by average marks
        comparisons.sort(key=lambda x: x['average_marks'], reverse=True)
        
        return {
            'total_subjects': len(comparisons),
            'easiest_subject': comparisons[0]['subject_name'] if comparisons else None,
            'hardest_subject': comparisons[-1]['subject_name'] if comparisons else None,
            'subject_rankings': comparisons,
            'average_overall_pass_rate': round(mean([c['pass_rate'] for c in comparisons]), 2) if comparisons else 0
        }
    
    @staticmethod
    def _get_difficulty_score(avg_marks: float, pass_rate: float) -> int:
        """Get difficulty score (1-5) based on metrics."""
        if avg_marks >= 75 and pass_rate >= 90:
            return 1  # Easy
        elif avg_marks >= 65 and pass_rate >= 80:
            return 2  # Moderate
        elif avg_marks >= 55 and pass_rate >= 70:
            return 3  # Moderate-Hard
        elif avg_marks >= 45 and pass_rate >= 60:
            return 4  # Hard
        else:
            return 5  # Very Hard
    
    @staticmethod
    def identify_trending_subjects(semester_wise_data: Dict[int, Dict[str, List[Dict[str, Any]]]]) -> Dict[str, Any]:
        """
        Identify subjects with improving or declining trends across semesters.
        
        Args:
            semester_wise_data: Nested dict: {semester: {subject_name: [marks]}}
            
        Returns:
            Trending analysis
        """
        if not semester_wise_data:
            return {'trends': 'No data'}
        
        # Track each subject across semesters
        subject_trends = defaultdict(list)
        
        for semester in sorted(semester_wise_data.keys()):
            for subject_name, marks_data in semester_wise_data[semester].items():
                if marks_data:
                    marks_values = [float(m['marks_obtained']) for m in marks_data]
                    avg = mean(marks_values)
                    subject_trends[subject_name].append({
                        'semester': semester,
                        'average': avg
                    })
        
        trending_analysis = []
        
        for subject_name, trend_data in subject_trends.items():
            if len(trend_data) >= 2:
                # Compare first and last semester
                first_avg = trend_data[0]['average']
                last_avg = trend_data[-1]['average']
                change = last_avg - first_avg
                change_pct = (change / first_avg * 100) if first_avg > 0 else 0
                
                if change > 5:
                    trend = "Improving"
                elif change < -5:
                    trend = "Declining"
                else:
                    trend = "Stable"
                
                trending_analysis.append({
                    'subject_name': subject_name,
                    'trend': trend,
                    'change': round(change, 2),
                    'change_percentage': round(change_pct, 2),
                    'semesters_tracked': len(trend_data),
                    'first_average': round(first_avg, 2),
                    'last_average': round(last_avg, 2)
                })
        
        # Sort by change percentage
        trending_analysis.sort(key=lambda x: abs(x['change_percentage']), reverse=True)
        
        improving = [s for s in trending_analysis if s['trend'] == 'Improving']
        declining = [s for s in trending_analysis if s['trend'] == 'Declining']
        
        return {
            'total_subjects_tracked': len(trending_analysis),
            'improving_subjects': len(improving),
            'declining_subjects': len(declining),
            'stable_subjects': len(trending_analysis) - len(improving) - len(declining),
            'most_improved': improving[0] if improving else None,
            'most_declined': declining[0] if declining else None,
            'all_trends': trending_analysis
        }
    
    @staticmethod
    def analyze_backlog_patterns(backlog_data: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Analyze patterns in subject backlogs.
        
        Args:
            backlog_data: List of backlog records from detect_backlogs()
            
        Returns:
            Backlog pattern analysis
        """
        if not backlog_data:
            return {
                'total_backlogs': 0,
                'status': 'No backlogs - excellent performance!'
            }
        
        # Group by subject
        subject_backlogs = defaultdict(list)
        student_backlogs = defaultdict(list)
        semester_backlogs = defaultdict(int)
        
        for record in backlog_data:
            subject_id = record['subject_id']
            student_id = record['student_id']
            semester = record['semester']
            
            subject_backlogs[record['subject_name']].append(record)
            student_backlogs[record['student_name']].append(record)
            semester_backlogs[semester] += 1
        
        # Most problematic subjects
        problematic_subjects = sorted(
            [{'subject': name, 'backlog_count': len(records)} 
             for name, records in subject_backlogs.items()],
            key=lambda x: x['backlog_count'],
            reverse=True
        )
        
        # Students with most backlogs
        at_risk_students = sorted(
            [{'student': name, 'backlog_count': len(records)} 
             for name, records in student_backlogs.items()],
            key=lambda x: x['backlog_count'],
            reverse=True
        )
        
        return {
            'total_backlogs': len(backlog_data),
            'total_subjects_with_backlogs': len(subject_backlogs),
            'total_students_with_backlogs': len(student_backlogs),
            'most_problematic_subject': problematic_subjects[0]['subject'] if problematic_subjects else None,
            'problematic_subjects_top5': problematic_subjects[:5],
            'students_at_risk_top10': at_risk_students[:10],
            'semester_distribution': dict(semester_backlogs),
            'severity': SubjectAnalytics._assess_backlog_severity(len(backlog_data), len(student_backlogs))
        }
    
    @staticmethod
    def _assess_backlog_severity(total_backlogs: int, total_students: int) -> str:
        """Assess overall backlog severity."""
        if total_backlogs == 0:
            return "None"
        
        avg_backlogs = total_backlogs / total_students if total_students > 0 else 0
        
        if avg_backlogs > 3:
            return "Critical - Immediate intervention needed"
        elif avg_backlogs > 2:
            return "High - Requires attention"
        elif avg_backlogs > 1:
            return "Moderate - Monitor closely"
        else:
            return "Low - Manageable"
    
    @staticmethod
    def generate_subject_recommendations(difficulty_analysis: Dict[str, Any], 
                                        backlog_analysis: Dict[str, Any]) -> Dict[str, Any]:
        """
        Generate recommendations for subject improvement.
        
        Args:
            difficulty_analysis: Subject difficulty metrics
            backlog_analysis: Backlog pattern analysis
            
        Returns:
            Subject improvement recommendations
        """
        recommendations = []
        priority_actions = []
        
        difficulty = difficulty_analysis.get('difficulty', 'Unknown')
        pass_rate = difficulty_analysis.get('pass_rate', 0)
        total_backlogs = backlog_analysis.get('total_backlogs', 0)
        
        # Difficulty-based recommendations
        if difficulty in ['Hard', 'Very Hard']:
            recommendations.append("Subject is challenging - consider additional support materials")
            priority_actions.append("Schedule extra tutorial sessions")
        
        # Pass rate recommendations
        if pass_rate < 60:
            recommendations.append("Low pass rate - teaching approach may need revision")
            priority_actions.append("Review curriculum and teaching methodology")
        elif pass_rate < 75:
            recommendations.append("Pass rate is acceptable but has room for improvement")
        
        # Backlog recommendations
        if total_backlogs > 10:
            recommendations.append("High number of backlogs - immediate intervention required")
            priority_actions.append("Organize remedial classes for struggling students")
        elif total_backlogs > 0:
            recommendations.append("Some students have backlogs - provide targeted support")
        
        # Consistency recommendations
        std_dev = difficulty_analysis.get('std_deviation', 0)
        if std_dev > 20:
            recommendations.append("High performance variation - students need more uniform support")
            priority_actions.append("Identify and support students lagging behind")
        
        return {
            'subject_name': difficulty_analysis.get('subject_name', 'Unknown'),
            'overall_health': 'Good' if pass_rate >= 75 and total_backlogs < 5 else 'Needs Improvement',
            'recommendations': recommendations if recommendations else ['Subject performance is satisfactory'],
            'priority_actions': priority_actions if priority_actions else ['Continue current teaching approach'],
            'monitoring_required': pass_rate < 70 or total_backlogs > 5
        }
