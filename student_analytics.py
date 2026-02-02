"""
Student analytics module.
Processes student-related data - no database access.
Performs calculations, trends analysis, and comparisons.
"""

from typing import List, Dict, Any, Optional
from statistics import mean, median, stdev
from collections import defaultdict


class StudentAnalytics:
    """Analyzes student performance data."""
    
    @staticmethod
    def analyze_performance_report(report: Dict[str, Any]) -> Dict[str, Any]:
        """
        Analyze student performance report with insights.
        
        Args:
            report: Raw performance report from database
            
        Returns:
            Enhanced report with analysis and insights
        """
        if not report:
            return {}
        
        cgpa = float(report.get('current_cgpa', 0))
        avg_marks = float(report.get('overall_average', 0))
        backlogs = int(report.get('total_backlogs', 0))
        
        # Performance category
        if cgpa >= 9.0:
            category = "Excellent"
        elif cgpa >= 8.0:
            category = "Very Good"
        elif cgpa >= 7.0:
            category = "Good"
        elif cgpa >= 6.0:
            category = "Average"
        else:
            category = "Below Average"
        
        # Risk assessment
        risk_level = "Low"
        if backlogs > 5:
            risk_level = "High"
        elif backlogs > 2:
            risk_level = "Medium"
        
        return {
            **report,
            'performance_category': category,
            'risk_level': risk_level,
            'is_at_risk': backlogs > 2,
            'needs_attention': avg_marks < 50 or backlogs > 0
        }
    
    @staticmethod
    def analyze_semester_trend(semester_data: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Analyze semester-wise performance trends.
        
        Args:
            semester_data: List of semester performance records
            
        Returns:
            Trend analysis with insights
        """
        if not semester_data:
            return {
                'trend': 'No data',
                'is_improving': False,
                'consistency': 'Unknown'
            }
        
        # Sort by semester
        sorted_data = sorted(semester_data, key=lambda x: x['semester'])
        
        # Extract GPAs
        gpas = [float(rec['semester_gpa']) for rec in sorted_data]
        avg_marks_list = [float(rec['average_marks']) for rec in sorted_data]
        backlogs_list = [int(rec['backlogs']) for rec in sorted_data]
        
        # Trend detection
        if len(gpas) < 2:
            trend = "Insufficient data"
            is_improving = False
        else:
            # Compare first half with second half
            mid = len(gpas) // 2
            first_half_avg = mean(gpas[:mid]) if mid > 0 else gpas[0]
            second_half_avg = mean(gpas[mid:])
            
            if second_half_avg > first_half_avg + 0.3:
                trend = "Improving"
                is_improving = True
            elif second_half_avg < first_half_avg - 0.3:
                trend = "Declining"
                is_improving = False
            else:
                trend = "Stable"
                is_improving = False
        
        # Consistency check
        if len(gpas) > 1:
            gpa_std = stdev(gpas) if len(gpas) > 1 else 0
            if gpa_std < 0.3:
                consistency = "High"
            elif gpa_std < 0.6:
                consistency = "Moderate"
            else:
                consistency = "Low"
        else:
            consistency = "Unknown"
        
        return {
            'total_semesters': len(sorted_data),
            'trend': trend,
            'is_improving': is_improving,
            'consistency': consistency,
            'average_gpa': round(mean(gpas), 2),
            'highest_gpa': round(max(gpas), 2),
            'lowest_gpa': round(min(gpas), 2),
            'total_backlogs': sum(backlogs_list),
            'semesters_with_backlogs': sum(1 for b in backlogs_list if b > 0),
            'semester_details': sorted_data
        }
    
    @staticmethod
    def analyze_subject_performance(marks_trend: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Analyze subject-wise performance trends.
        
        Args:
            marks_trend: List of subject marks across semesters
            
        Returns:
            Subject performance analysis
        """
        if not marks_trend:
            return {'subjects': [], 'summary': 'No data'}
        
        # Group by subject
        subject_data = defaultdict(list)
        for record in marks_trend:
            subject_id = record['subject_id']
            subject_data[subject_id].append({
                'subject_name': record['subject_name'],
                'semester': record['semester'],
                'marks': float(record['marks_obtained'])
            })
        
        # Analyze each subject
        subject_analysis = []
        for subject_id, records in subject_data.items():
            sorted_records = sorted(records, key=lambda x: x['semester'])
            marks_list = [r['marks'] for r in sorted_records]
            
            # Trend for this subject
            if len(marks_list) > 1:
                if marks_list[-1] > marks_list[0] + 5:
                    trend = "Improving"
                elif marks_list[-1] < marks_list[0] - 5:
                    trend = "Declining"
                else:
                    trend = "Stable"
            else:
                trend = "Single attempt"
            
            subject_analysis.append({
                'subject_id': subject_id,
                'subject_name': sorted_records[0]['subject_name'],
                'attempts': len(marks_list),
                'average_marks': round(mean(marks_list), 2),
                'highest_marks': round(max(marks_list), 2),
                'lowest_marks': round(min(marks_list), 2),
                'latest_marks': round(marks_list[-1], 2),
                'trend': trend,
                'is_passing': marks_list[-1] >= 40
            })
        
        # Sort by average marks
        subject_analysis.sort(key=lambda x: x['average_marks'], reverse=True)
        
        return {
            'total_subjects': len(subject_analysis),
            'subjects': subject_analysis,
            'best_subject': subject_analysis[0]['subject_name'] if subject_analysis else None,
            'worst_subject': subject_analysis[-1]['subject_name'] if subject_analysis else None
        }
    
    @staticmethod
    def identify_improvement_areas(weak_subjects: List[Dict[str, Any]], 
                                   threshold: float = 50.0) -> Dict[str, Any]:
        """
        Identify areas needing improvement.
        
        Args:
            weak_subjects: List of weak subject records
            threshold: Marks threshold used
            
        Returns:
            Improvement recommendations
        """
        if not weak_subjects:
            return {
                'needs_improvement': False,
                'weak_subjects': [],
                'priority_subjects': [],
                'recommendation': 'Performance is satisfactory across all subjects'
            }
        
        # Sort by severity (times below threshold and latest marks)
        sorted_subjects = sorted(
            weak_subjects, 
            key=lambda x: (x['times_below_threshold'], -x['latest_marks']), 
            reverse=True
        )
        
        # Identify critical subjects (repeated failure)
        critical = [s for s in sorted_subjects if s['times_below_threshold'] > 1]
        
        # Priority subjects (most recent failures)
        priority = sorted_subjects[:3]  # Top 3 weakest
        
        return {
            'needs_improvement': True,
            'total_weak_subjects': len(weak_subjects),
            'critical_subjects_count': len(critical),
            'weak_subjects': sorted_subjects,
            'priority_subjects': priority,
            'critical_subjects': critical,
            'recommendation': StudentAnalytics._generate_recommendation(sorted_subjects, critical)
        }
    
    @staticmethod
    def _generate_recommendation(weak_subjects: List[Dict[str, Any]], 
                                critical: List[Dict[str, Any]]) -> str:
        """Generate improvement recommendation text."""
        if len(critical) > 0:
            return f"URGENT: Focus on {len(critical)} critical subjects with repeated failures. Consider tutoring or extra study sessions."
        elif len(weak_subjects) > 3:
            return f"Focus needed in {len(weak_subjects)} subjects. Prioritize the weakest areas first."
        else:
            return f"Minor improvement needed in {len(weak_subjects)} subjects. Consistent effort should help."
    
    @staticmethod
    def compare_with_average(student_marks: List[Dict[str, Any]], 
                            all_students_marks: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Compare student performance with class average.
        
        Args:
            student_marks: Student's marks
            all_students_marks: All students' marks for comparison
            
        Returns:
            Comparison analysis
        """
        if not student_marks or not all_students_marks:
            return {'comparison': 'Insufficient data'}
        
        student_avg = mean([float(m['marks_obtained']) for m in student_marks])
        class_avg = mean([float(m['marks_obtained']) for m in all_students_marks])
        
        diff = student_avg - class_avg
        diff_pct = (diff / class_avg * 100) if class_avg > 0 else 0
        
        if diff > 10:
            performance = "Above Average"
        elif diff < -10:
            performance = "Below Average"
        else:
            performance = "Average"
        
        return {
            'student_average': round(student_avg, 2),
            'class_average': round(class_avg, 2),
            'difference': round(diff, 2),
            'difference_percentage': round(diff_pct, 2),
            'performance_level': performance,
            'percentile_estimate': StudentAnalytics._estimate_percentile(student_avg, all_students_marks)
        }
    
    @staticmethod
    def _estimate_percentile(student_avg: float, all_marks: List[Dict[str, Any]]) -> float:
        """Estimate student's percentile based on average marks."""
        all_averages = defaultdict(list)
        
        # Group by student and calculate average
        for mark in all_marks:
            all_averages[mark['student_id']].append(float(mark['marks_obtained']))
        
        student_averages = [mean(marks) for marks in all_averages.values()]
        student_averages.sort()
        
        # Find position
        below = sum(1 for avg in student_averages if avg < student_avg)
        percentile = (below / len(student_averages) * 100) if student_averages else 50
        
        return round(percentile, 1)
    
    @staticmethod
    def predict_cgpa(semester_data: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Predict next semester CGPA based on trends.
        
        Args:
            semester_data: List of semester performance records
            
        Returns:
            CGPA prediction
        """
        if len(semester_data) < 2:
            return {
                'predicted_cgpa': None,
                'confidence': 'Low',
                'note': 'Need at least 2 semesters for prediction'
            }
        
        sorted_data = sorted(semester_data, key=lambda x: x['semester'])
        recent_gpas = [float(rec['semester_gpa']) for rec in sorted_data[-3:]]  # Last 3 semesters
        
        # Simple moving average prediction
        predicted = mean(recent_gpas)
        
        # Adjust based on trend
        if len(recent_gpas) >= 2:
            trend = recent_gpas[-1] - recent_gpas[-2]
            predicted += (trend * 0.5)  # 50% of the trend
        
        # Confidence based on consistency
        if len(recent_gpas) > 1:
            std = stdev(recent_gpas)
            if std < 0.3:
                confidence = "High"
            elif std < 0.6:
                confidence = "Medium"
            else:
                confidence = "Low"
        else:
            confidence = "Low"
        
        # Bounds
        predicted = max(0.0, min(10.0, predicted))
        
        return {
            'predicted_cgpa': round(predicted, 2),
            'current_cgpa': round(recent_gpas[-1], 2),
            'confidence': confidence,
            'based_on_semesters': len(recent_gpas)
        }
    
    @staticmethod
    def analyze_doubt_patterns(doubts: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Analyze student's doubt-raising patterns.
        
        Args:
            doubts: List of doubt records
            
        Returns:
            Doubt pattern analysis
        """
        if not doubts:
            return {
                'total_doubts': 0,
                'engagement': 'Low',
                'note': 'No doubts raised - may need encouragement to ask questions'
            }
        
        total = len(doubts)
        answered = sum(1 for d in doubts if d['status'] == 'ANSWERED')
        pending = total - answered
        
        # Engagement level
        if total > 10:
            engagement = "High"
        elif total > 5:
            engagement = "Moderate"
        else:
            engagement = "Low"
        
        response_rate = (answered / total * 100) if total > 0 else 0
        
        return {
            'total_doubts': total,
            'answered': answered,
            'pending': pending,
            'response_rate': round(response_rate, 2),
            'engagement': engagement,
            'average_doubts_per_semester': round(total / 8, 2) if total > 0 else 0  # Assuming 8 semesters max
        }
