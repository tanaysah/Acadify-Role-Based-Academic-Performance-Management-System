"""
Teacher analytics module.
Processes teacher and class performance data - no database access.
Analyzes teaching effectiveness and subject outcomes.
"""

from typing import List, Dict, Any
from statistics import mean, median, stdev
from collections import defaultdict


class TeacherAnalytics:
    """Analyzes teacher and class performance data."""
    
    @staticmethod
    def analyze_teacher_performance(teacher_subjects: List[Dict[str, Any]], 
                                   subject_marks: Dict[int, List[Dict[str, Any]]]) -> Dict[str, Any]:
        """
        Analyze overall teacher performance across all subjects.
        
        Args:
            teacher_subjects: List of subjects taught by teacher
            subject_marks: Dictionary mapping subject_id to marks data
            
        Returns:
            Teacher performance analysis
        """
        if not teacher_subjects:
            return {
                'total_subjects': 0,
                'overall_effectiveness': 'No data'
            }
        
        subject_analyses = []
        
        for subject in teacher_subjects:
            subject_id = subject['subject_id']
            marks = subject_marks.get(subject_id, [])
            
            if marks:
                marks_values = [float(m['marks_obtained']) for m in marks]
                pass_count = sum(1 for m in marks_values if m >= 40)
                
                subject_analyses.append({
                    'subject_name': subject['subject_name'],
                    'semester': subject['semester'],
                    'total_students': len(marks),
                    'average_marks': round(mean(marks_values), 2),
                    'pass_rate': round((pass_count / len(marks) * 100), 2) if marks else 0,
                    'highest_marks': round(max(marks_values), 2),
                    'lowest_marks': round(min(marks_values), 2)
                })
        
        if not subject_analyses:
            return {
                'total_subjects': len(teacher_subjects),
                'overall_effectiveness': 'No student data available'
            }
        
        # Overall metrics
        avg_pass_rate = mean([s['pass_rate'] for s in subject_analyses])
        avg_marks = mean([s['average_marks'] for s in subject_analyses])
        
        # Effectiveness rating
        if avg_pass_rate >= 90 and avg_marks >= 70:
            effectiveness = "Excellent"
        elif avg_pass_rate >= 75 and avg_marks >= 60:
            effectiveness = "Very Good"
        elif avg_pass_rate >= 60 and avg_marks >= 50:
            effectiveness = "Good"
        elif avg_pass_rate >= 40:
            effectiveness = "Average"
        else:
            effectiveness = "Needs Improvement"
        
        return {
            'total_subjects': len(teacher_subjects),
            'subjects_analyzed': len(subject_analyses),
            'overall_pass_rate': round(avg_pass_rate, 2),
            'overall_average_marks': round(avg_marks, 2),
            'overall_effectiveness': effectiveness,
            'subject_performance': subject_analyses,
            'best_performing_subject': max(subject_analyses, key=lambda x: x['pass_rate'])['subject_name'] if subject_analyses else None,
            'needs_attention_subject': min(subject_analyses, key=lambda x: x['pass_rate'])['subject_name'] if subject_analyses else None
        }
    
    @staticmethod
    def analyze_student_distribution(students: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Analyze distribution of students taught by teacher.
        
        Args:
            students: List of students taught
            
        Returns:
            Student distribution analysis
        """
        if not students:
            return {
                'total_students': 0,
                'distribution': []
            }
        
        # Group by stream
        stream_dist = defaultdict(int)
        subject_dist = defaultdict(int)
        semester_dist = defaultdict(int)
        
        for student in students:
            stream_dist[student.get('stream', 'Unknown')] += 1
            subject_dist[student.get('subject_name', 'Unknown')] += 1
            semester_dist[student.get('semester', 0)] += 1
        
        return {
            'total_students': len(students),
            'unique_students': len(set(s['student_id'] for s in students)),
            'stream_distribution': dict(stream_dist),
            'subject_distribution': dict(subject_dist),
            'semester_distribution': dict(semester_dist)
        }
    
    @staticmethod
    def analyze_doubt_handling(doubts: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Analyze teacher's doubt handling performance.
        
        Args:
            doubts: List of doubts assigned to teacher
            
        Returns:
            Doubt handling analysis
        """
        if not doubts:
            return {
                'total_doubts': 0,
                'response_quality': 'No doubts assigned',
                'engagement': 'None'
            }
        
        total = len(doubts)
        answered = sum(1 for d in doubts if d['status'] == 'ANSWERED')
        pending = total - answered
        
        response_rate = (answered / total * 100) if total > 0 else 0
        
        # Quality rating based on response rate
        if response_rate >= 90:
            quality = "Excellent"
            engagement = "High"
        elif response_rate >= 70:
            quality = "Good"
            engagement = "Moderate"
        elif response_rate >= 50:
            quality = "Average"
            engagement = "Moderate"
        else:
            quality = "Needs Improvement"
            engagement = "Low"
        
        # Calculate average response time (if created_at available)
        # This is a simplified version - actual implementation would need datetime parsing
        
        return {
            'total_doubts': total,
            'answered': answered,
            'pending': pending,
            'response_rate': round(response_rate, 2),
            'response_quality': quality,
            'engagement': engagement,
            'pending_urgent': pending  # All pending could be considered urgent
        }
    
    @staticmethod
    def compare_with_peers(teacher_performance: Dict[str, Any], 
                          all_teachers_data: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Compare teacher performance with peer average.
        
        Args:
            teacher_performance: This teacher's performance metrics
            all_teachers_data: All teachers' performance data
            
        Returns:
            Peer comparison analysis
        """
        if not all_teachers_data:
            return {'comparison': 'No peer data available'}
        
        teacher_pass_rate = teacher_performance.get('overall_pass_rate', 0)
        teacher_avg_marks = teacher_performance.get('overall_average_marks', 0)
        
        peer_pass_rates = [t['overall_pass_rate'] for t in all_teachers_data 
                          if 'overall_pass_rate' in t]
        peer_avg_marks = [t['overall_average_marks'] for t in all_teachers_data 
                         if 'overall_average_marks' in t]
        
        if not peer_pass_rates or not peer_avg_marks:
            return {'comparison': 'Insufficient peer data'}
        
        dept_avg_pass_rate = mean(peer_pass_rates)
        dept_avg_marks = mean(peer_avg_marks)
        
        pass_rate_diff = teacher_pass_rate - dept_avg_pass_rate
        marks_diff = teacher_avg_marks - dept_avg_marks
        
        # Ranking
        better_than = sum(1 for rate in peer_pass_rates if teacher_pass_rate > rate)
        percentile = (better_than / len(peer_pass_rates) * 100) if peer_pass_rates else 50
        
        if percentile >= 75:
            standing = "Top Performer"
        elif percentile >= 50:
            standing = "Above Average"
        elif percentile >= 25:
            standing = "Average"
        else:
            standing = "Below Average"
        
        return {
            'teacher_pass_rate': round(teacher_pass_rate, 2),
            'department_avg_pass_rate': round(dept_avg_pass_rate, 2),
            'pass_rate_difference': round(pass_rate_diff, 2),
            'teacher_avg_marks': round(teacher_avg_marks, 2),
            'department_avg_marks': round(dept_avg_marks, 2),
            'marks_difference': round(marks_diff, 2),
            'percentile': round(percentile, 1),
            'standing': standing
        }
    
    @staticmethod
    def identify_struggling_students(students_marks: List[Dict[str, Any]], 
                                    threshold: float = 40.0) -> Dict[str, Any]:
        """
        Identify students struggling in teacher's subjects.
        
        Args:
            students_marks: Marks data for students
            threshold: Failing threshold
            
        Returns:
            Struggling students analysis
        """
        if not students_marks:
            return {
                'struggling_students': [],
                'total_struggling': 0
            }
        
        # Group by student
        student_data = defaultdict(list)
        for mark in students_marks:
            student_id = mark['student_id']
            student_data[student_id].append({
                'student_name': mark.get('student_name', 'Unknown'),
                'roll_number': mark.get('roll_number', 'N/A'),
                'subject_name': mark.get('subject_name', 'Unknown'),
                'marks': float(mark['marks_obtained']),
                'semester': mark.get('semester', 0)
            })
        
        struggling = []
        for student_id, marks in student_data.items():
            failing_subjects = [m for m in marks if m['marks'] < threshold]
            
            if failing_subjects:
                avg_marks = mean([m['marks'] for m in marks])
                struggling.append({
                    'student_id': student_id,
                    'student_name': marks[0]['student_name'],
                    'roll_number': marks[0]['roll_number'],
                    'failing_subjects_count': len(failing_subjects),
                    'average_marks': round(avg_marks, 2),
                    'failing_subjects': [s['subject_name'] for s in failing_subjects],
                    'risk_level': 'High' if len(failing_subjects) > 2 else 'Medium'
                })
        
        # Sort by number of failing subjects
        struggling.sort(key=lambda x: x['failing_subjects_count'], reverse=True)
        
        return {
            'total_struggling': len(struggling),
            'high_risk_count': sum(1 for s in struggling if s['risk_level'] == 'High'),
            'struggling_students': struggling,
            'intervention_needed': len(struggling) > 0
        }
    
    @staticmethod
    def analyze_grade_distribution(marks_data: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Analyze grade distribution for teacher's classes.
        
        Args:
            marks_data: List of marks records
            
        Returns:
            Grade distribution analysis
        """
        if not marks_data:
            return {
                'distribution': {},
                'note': 'No data available'
            }
        
        marks_values = [float(m['marks_obtained']) for m in marks_data]
        
        # Grade ranges
        grades = {
            'A+ (90-100)': sum(1 for m in marks_values if m >= 90),
            'A (80-89)': sum(1 for m in marks_values if 80 <= m < 90),
            'B+ (70-79)': sum(1 for m in marks_values if 70 <= m < 80),
            'B (60-69)': sum(1 for m in marks_values if 60 <= m < 70),
            'C (50-59)': sum(1 for m in marks_values if 50 <= m < 60),
            'D (40-49)': sum(1 for m in marks_values if 40 <= m < 50),
            'F (<40)': sum(1 for m in marks_values if m < 40)
        }
        
        total = len(marks_values)
        grade_percentages = {
            grade: round((count / total * 100), 2) if total > 0 else 0
            for grade, count in grades.items()
        }
        
        # Distribution quality
        excellent_count = grades['A+ (90-100)'] + grades['A (80-89)']
        fail_count = grades['F (<40)']
        
        if excellent_count / total > 0.3 and fail_count / total < 0.1:
            quality = "Excellent distribution - majority performing well"
        elif fail_count / total > 0.3:
            quality = "Concerning - high failure rate"
        else:
            quality = "Normal distribution"
        
        return {
            'total_students': total,
            'distribution_counts': grades,
            'distribution_percentages': grade_percentages,
            'mean_marks': round(mean(marks_values), 2),
            'median_marks': round(median(marks_values), 2),
            'std_deviation': round(stdev(marks_values), 2) if len(marks_values) > 1 else 0,
            'quality_assessment': quality
        }
    
    @staticmethod
    def generate_teaching_insights(performance: Dict[str, Any], 
                                  doubts: Dict[str, Any], 
                                  struggling: Dict[str, Any]) -> Dict[str, Any]:
        """
        Generate comprehensive teaching insights and recommendations.
        
        Args:
            performance: Teacher performance metrics
            doubts: Doubt handling metrics
            struggling: Struggling students data
            
        Returns:
            Teaching insights and recommendations
        """
        insights = []
        recommendations = []
        
        # Performance insights
        pass_rate = performance.get('overall_pass_rate', 0)
        if pass_rate >= 90:
            insights.append("Excellent pass rate - teaching methods are highly effective")
        elif pass_rate < 60:
            insights.append("Pass rate needs improvement")
            recommendations.append("Review teaching methodology and consider additional student support")
        
        # Doubt handling insights
        doubt_response_rate = doubts.get('response_rate', 0)
        if doubt_response_rate < 70:
            insights.append("Doubt response rate is below optimal")
            recommendations.append("Prioritize timely responses to student queries")
        elif doubt_response_rate >= 90:
            insights.append("Excellent student engagement and doubt resolution")
        
        # Struggling students insights
        struggling_count = struggling.get('total_struggling', 0)
        high_risk = struggling.get('high_risk_count', 0)
        
        if high_risk > 0:
            insights.append(f"{high_risk} students at high risk of failure")
            recommendations.append(f"Schedule one-on-one sessions with {high_risk} high-risk students")
        
        if struggling_count > 0:
            recommendations.append("Consider extra tutorial sessions for struggling students")
        
        # Overall assessment
        effectiveness = performance.get('overall_effectiveness', 'Unknown')
        
        return {
            'effectiveness_rating': effectiveness,
            'key_insights': insights,
            'recommendations': recommendations,
            'strengths': TeacherAnalytics._identify_strengths(performance, doubts),
            'areas_for_improvement': TeacherAnalytics._identify_improvements(performance, doubts, struggling)
        }
    
    @staticmethod
    def _identify_strengths(performance: Dict[str, Any], doubts: Dict[str, Any]) -> List[str]:
        """Identify teacher's strengths."""
        strengths = []
        
        if performance.get('overall_pass_rate', 0) >= 85:
            strengths.append("High student pass rate")
        
        if performance.get('overall_average_marks', 0) >= 70:
            strengths.append("Students achieving high average marks")
        
        if doubts.get('response_rate', 0) >= 85:
            strengths.append("Excellent student query resolution")
        
        if not strengths:
            strengths.append("Consistent teaching delivery")
        
        return strengths
    
    @staticmethod
    def _identify_improvements(performance: Dict[str, Any], doubts: Dict[str, Any], 
                              struggling: Dict[str, Any]) -> List[str]:
        """Identify areas for improvement."""
        improvements = []
        
        if performance.get('overall_pass_rate', 0) < 70:
            improvements.append("Increase overall pass rate")
        
        if doubts.get('response_rate', 0) < 75:
            improvements.append("Improve doubt response time")
        
        if struggling.get('high_risk_count', 0) > 0:
            improvements.append("Provide additional support to at-risk students")
        
        if performance.get('overall_average_marks', 0) < 60:
            improvements.append("Enhance student understanding and performance")
        
        if not improvements:
            improvements.append("Maintain current teaching excellence")
        
        return improvements
